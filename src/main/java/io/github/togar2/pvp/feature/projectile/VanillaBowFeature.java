package io.github.togar2.pvp.feature.projectile;

import io.github.togar2.pvp.enchantment.EnchantmentUtils;
import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.projectile.AbstractArrow;
import io.github.togar2.pvp.projectile.Arrow;
import io.github.togar2.pvp.projectile.SpectralArrow;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.ViewUtil;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class VanillaBowFeature implements BowFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaBowFeature> DEFINED = new DefinedFeature<>(
			FeatureType.BOW, VanillaBowFeature::new,
			FeatureType.ITEM_DAMAGE, FeatureType.VERSION
	);
	
	private final ItemDamageFeature itemDamageFeature;
	private final CombatVersion version;
	
	public VanillaBowFeature(FeatureConfiguration configuration) {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.version = configuration.get(FeatureType.VERSION);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerItemAnimationEvent.class, event -> {
			if (event.getItemAnimationType() == PlayerItemAnimationEvent.ItemAnimationType.BOW) {
				if (!event.getPlayer().isCreative()
						&& EntityUtils.getProjectile(event.getPlayer(), Arrow.ARROW_PREDICATE).first().isAir()) {
					event.setCancelled(true);
				}
			}
		});
		
		node.addListener(ItemUpdateStateEvent.class, event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			if (stack.material() != Material.BOW) return;
			
			boolean infinite = player.isCreative() || EnchantmentUtils.getLevel(Enchantment.INFINITY, stack) > 0;
			
			Pair<ItemStack, Integer> projectilePair = EntityUtils.getProjectile(player, Arrow.ARROW_PREDICATE);
			ItemStack projectile = projectilePair.first();
			int projectileSlot = projectilePair.second();
			
			if (!infinite && projectile.isAir()) return;
			if (projectile.isAir()) {
				projectile = Arrow.DEFAULT_ARROW;
				projectileSlot = -1;
			}
			
			long useDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
			double power = getBowPower(useDuration);
			if (power < 0.1) return;
			
			// Arrow creation
			AbstractArrow arrow = createArrow(projectile, player);
			
			if (power >= 1) arrow.setCritical(true);
			
			int powerEnchantment = EnchantmentUtils.getLevel(Enchantment.POWER, stack);
			if (powerEnchantment > 0)
				arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchantment * 0.5 + 0.5);
			
			int punchEnchantment = EnchantmentUtils.getLevel(Enchantment.PUNCH, stack);
			if (punchEnchantment > 0) arrow.setKnockback(punchEnchantment);
			
			if (EnchantmentUtils.getLevel(Enchantment.FLAME, stack) > 0)
				EntityUtils.setOnFireForSeconds(arrow, 100);
			
			itemDamageFeature.damageEquipment(player, event.getHand() == Player.Hand.MAIN ?
					EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, 1);
			
			boolean reallyInfinite = infinite && projectile.material() == Material.ARROW;
			if (reallyInfinite || player.isCreative()
					&& (projectile.material() == Material.SPECTRAL_ARROW
					|| projectile.material() == Material.TIPPED_ARROW)) {
				arrow.setPickupMode(AbstractArrow.PickupMode.CREATIVE_ONLY);
			}
			
			// Arrow shooting
			Pos position = player.getPosition().add(0D, player.getEyeHeight() - 0.1, 0D);
			arrow.shootFromRotation(position.pitch(), position.yaw(), 0 , power * 3, 1.0);
			Vec playerVel = player.getVelocity();
			arrow.setVelocity(arrow.getVelocity().add(playerVel.x(),
					player.isOnGround() ? 0.0D : playerVel.y(), playerVel.z()));
			arrow.setInstance(Objects.requireNonNull(player.getInstance()), position);
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
					SoundEvent.ENTITY_ARROW_SHOOT, Sound.Source.PLAYER,
					1.0f, 1.0f / (random.nextFloat() * 0.4f + 1.2f) + (float) power * 0.5f
			), player);
			
			if (!reallyInfinite && !player.isCreative() && projectileSlot >= 0) {
				player.getInventory().setItemStack(projectileSlot,
						projectile.withAmount(projectile.amount() - 1));
			}
		});
	}
	
	protected double getBowPower(long useDurationMillis) {
		double seconds = useDurationMillis / 1000.0;
		double power = (seconds * seconds + seconds * 2.0) / 3.0;
		if (power > 1) {
			power = 1;
		}
		
		return power;
	}
	
	protected AbstractArrow createArrow(ItemStack stack, @Nullable Entity shooter) {
		if (stack.material() == Material.SPECTRAL_ARROW) {
			return new SpectralArrow(shooter);
		} else {
			Arrow arrow = new Arrow(shooter, version.legacy());
			arrow.inheritEffects(stack);
			return arrow;
		}
	}
}

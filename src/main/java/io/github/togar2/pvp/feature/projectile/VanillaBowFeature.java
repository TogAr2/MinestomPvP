package io.github.togar2.pvp.feature.projectile;

import io.github.togar2.pvp.entity.projectile.AbstractArrow;
import io.github.togar2.pvp.entity.projectile.Arrow;
import io.github.togar2.pvp.entity.projectile.SpectralArrow;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.effect.EffectFeature;
import io.github.togar2.pvp.feature.enchantment.EnchantmentFeature;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.player.Tracker;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class VanillaBowFeature implements BowFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaBowFeature> DEFINED = new DefinedFeature<>(
			FeatureType.BOW, VanillaBowFeature::new,
			FeatureType.ITEM_DAMAGE, FeatureType.EFFECT, FeatureType.ENCHANTMENT, FeatureType.PROJECTILE_ITEM
	);
	
	private final FeatureConfiguration configuration;
	
	private ItemDamageFeature itemDamageFeature;
	private EffectFeature effectFeature;
	private EnchantmentFeature enchantmentFeature;
	private ProjectileItemFeature projectileItemFeature;
	
	public VanillaBowFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void initDependencies() {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.effectFeature = configuration.get(FeatureType.EFFECT);
		this.enchantmentFeature = configuration.get(FeatureType.ENCHANTMENT);
		this.projectileItemFeature = configuration.get(FeatureType.PROJECTILE_ITEM);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerItemAnimationEvent.class, event -> {
			if (event.getItemAnimationType() == PlayerItemAnimationEvent.ItemAnimationType.BOW) {
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE
						&& projectileItemFeature.getBowProjectile(event.getPlayer()) == null) {
					event.setCancelled(true);
				}
			}
		});
		
		node.addListener(ItemUpdateStateEvent.class, event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			if (stack.material() != Material.BOW) return;
			
			EnchantmentList enchantmentList = stack.get(ItemComponent.ENCHANTMENTS);
			assert enchantmentList != null;
			
			boolean infinite = player.getGameMode() == GameMode.CREATIVE
					|| enchantmentList.level(Enchantment.INFINITY) > 0;
			
			ItemStack projectileItem;
			int projectileSlot;
			
			ProjectileItemFeature.ProjectileItem projectile = projectileItemFeature.getBowProjectile(player);
			if (!infinite && projectile == null) return;
			if (projectile == null) {
				projectileItem = Arrow.DEFAULT_ARROW;
				projectileSlot = -1;
			} else {
				projectileItem = projectile.stack();
				projectileSlot = projectile.slot();
			}
			
			long useDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
			double power = getBowPower(useDuration);
			if (power < 0.1) return;
			
			// Arrow creation
			AbstractArrow arrow = createArrow(projectileItem, player);
			
			if (power >= 1) arrow.setCritical(true);
			
			int powerEnchantment = enchantmentList.level(Enchantment.POWER);
			if (powerEnchantment > 0)
				arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchantment * 0.5 + 0.5);
			
			int punchEnchantment = enchantmentList.level(Enchantment.PUNCH);
			if (punchEnchantment > 0) arrow.setKnockback(punchEnchantment);
			
			if (enchantmentList.level(Enchantment.FLAME) > 0)
				arrow.setFireTicksLeft(100 * ServerFlag.SERVER_TICKS_PER_SECOND); // 100 seconds
			
			itemDamageFeature.damageEquipment(player, event.getHand() == Player.Hand.MAIN ?
					EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, 1);
			
			boolean reallyInfinite = infinite && projectileItem.material() == Material.ARROW;
			if (reallyInfinite || player.getGameMode() == GameMode.CREATIVE
					&& (projectileItem.material() == Material.SPECTRAL_ARROW
					|| projectileItem.material() == Material.TIPPED_ARROW)) {
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
			
			if (!reallyInfinite && player.getGameMode() != GameMode.CREATIVE && projectileSlot >= 0) {
				player.getInventory().setItemStack(projectileSlot,
						projectileItem.withAmount(projectileItem.amount() - 1));
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
			return new SpectralArrow(shooter, enchantmentFeature);
		} else {
			Arrow arrow = new Arrow(shooter, effectFeature, enchantmentFeature);
			arrow.setItemStack(stack);
			return arrow;
		}
	}
}

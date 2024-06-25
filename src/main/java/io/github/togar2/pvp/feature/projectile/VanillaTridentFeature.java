package io.github.togar2.pvp.feature.projectile;

import io.github.togar2.pvp.enchantment.EnchantmentUtils;
import io.github.togar2.pvp.entity.PvpPlayer;
import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.projectile.ThrownTrident;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.FluidUtils;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class VanillaTridentFeature implements TridentFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaTridentFeature> DEFINED = new DefinedFeature<>(
			FeatureType.TRIDENT, VanillaTridentFeature::new,
			FeatureType.ITEM_DAMAGE, FeatureType.VERSION
	);
	
	private final ItemDamageFeature itemDamageFeature;
	private final CombatVersion version;
	
	public static final Tag<Long> RIPTIDE_START = Tag.Long("riptideStart");
	
	public VanillaTridentFeature(FeatureConfiguration configuration) {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.version = configuration.get(FeatureType.VERSION);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(ItemUpdateStateEvent.class, event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			if (stack.material() != Material.TRIDENT) return;
			
			long useDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
			int ticks = (int) ((useDuration / 1000.0) * 20);
			if (ticks < 10) return;
			
			int riptide = EnchantmentUtils.getLevel(Enchantment.RIPTIDE, stack);
			if (riptide > 0 && !FluidUtils.isTouchingWater(player)) return;
			
			itemDamageFeature.damageEquipment(player, event.getHand() == Player.Hand.MAIN ?
					EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, 1);
			
			if (riptide > 0) {
				applyRiptide(player, riptide);
			} else {
				ThrownTrident trident = new ThrownTrident(player, version.legacy(), stack);
				
				Pos position = player.getPosition().add(0, player.getEyeHeight() - 0.1, 0);
				trident.setInstance(Objects.requireNonNull(player.getInstance()), position);
				
				trident.shootFromRotation(position.pitch(), position.yaw(), 0, 2.5, 1.0);
				
				Vec playerVel = player.getVelocity();
				trident.setVelocity(trident.getVelocity().add(playerVel.x(),
						player.isOnGround() ? 0.0 : playerVel.y(), playerVel.z()));
				
				ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
						SoundEvent.ITEM_TRIDENT_THROW, Sound.Source.PLAYER,
						1.0f, 1.0f
				), trident);
				if (player.getGameMode() != GameMode.CREATIVE) player.setItemInHand(event.getHand(), stack.consume(1));
			}
		});
		
		node.addListener(PlayerTickEvent.class, event -> {
			if (event.getPlayer().getPlayerMeta().isInRiptideSpinAttack()) {
				Player player = event.getPlayer();
				long ticks = player.getAliveTicks() - player.getTag(RIPTIDE_START);
				AtomicBoolean stopRiptide = new AtomicBoolean(ticks >= 20);
				
				assert player.getInstance() != null;
				player.getInstance().getEntityTracker().nearbyEntities(player.getPosition(), 5,
						EntityTracker.Target.ENTITIES, entity -> {
							if (entity != player && !stopRiptide.get() && entity instanceof LivingEntity
									&& entity.getBoundingBox().intersectEntity(entity.getPosition(), player)) {
								stopRiptide.set(true);
								
								var attackEvent = new EntityAttackEvent(player, entity);
								EventDispatcher.call(attackEvent);
								if (player instanceof PvpPlayer pvpPlayer)
									pvpPlayer.setVelocityNoUpdate(velocity -> velocity.mul(-0.2));
							}
						});
				
				//TODO detect player bouncing against wall
				
				if (stopRiptide.get())
					event.getPlayer().refreshActiveHand(false, false, false);
			}
		});
	}
	
	@Override
	public void applyRiptide(Player player, int level) {
		float yaw = player.getPosition().yaw();
		float pitch = player.getPosition().pitch();
		double h = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		double k = -Math.sin(Math.toRadians(pitch));
		double l = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		double length = Math.sqrt(h * h + k * k + l * l);
		double n = 3.0 * ((1.0 + level) / 4.0);
		
		player.setTag(RIPTIDE_START, player.getAliveTicks());
		player.setVelocity(player.getVelocity().add(new Vec(
				h * (n / length),
				k * (n / length),
				l * (n / length)
		).mul(ServerFlag.SERVER_TICKS_PER_SECOND)));
		
		SoundEvent soundEvent = level >= 3 ? SoundEvent.ITEM_TRIDENT_RIPTIDE_3 :
				(level == 2 ? SoundEvent.ITEM_TRIDENT_RIPTIDE_2 : SoundEvent.ITEM_TRIDENT_RIPTIDE_1);
		ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
				soundEvent, Sound.Source.PLAYER,
				1.0f, 1.0f
		), player);
		
		player.scheduleNextTick(entity -> player.refreshActiveHand(false, false, true));
	}
}

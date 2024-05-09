package io.github.togar2.pvp.feature.damage;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.events.EntityPreDeathEvent;
import io.github.togar2.pvp.events.FinalDamageEvent;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.armor.ArmorFeature;
import io.github.togar2.pvp.feature.block.BlockFeature;
import io.github.togar2.pvp.feature.food.ExhaustionFeature;
import io.github.togar2.pvp.feature.knockback.KnockbackFeature;
import io.github.togar2.pvp.feature.provider.ProviderForEntity;
import io.github.togar2.pvp.feature.totem.TotemFeature;
import io.github.togar2.pvp.feature.tracking.TrackingFeature;
import io.github.togar2.pvp.listeners.DamageHandler;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.ItemUtils;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.Difficulty;

import java.util.Objects;

public class VanillaDamageFeature implements DamageFeature, RegistrableFeature {
	public static final Tag<Long> NEW_DAMAGE_TIME = Tag.Long("newDamageTime");
	public static final Tag<Float> LAST_DAMAGE_AMOUNT = Tag.Float("lastDamageAmount");
	
	private final ProviderForEntity<Difficulty> difficultyProvider;
	
	private final BlockFeature blockFeature;
	private final ArmorFeature armorFeature;
	private final TotemFeature totemFeature;
	private final ExhaustionFeature exhaustionFeature;
	private final KnockbackFeature knockbackFeature;
	private final TrackingFeature trackingFeature;
	
	private final CombatVersion version;
	
	public VanillaDamageFeature(ProviderForEntity<Difficulty> difficultyProvider, BlockFeature blockFeature,
	                            ArmorFeature armorFeature, TotemFeature totemFeature, ExhaustionFeature exhaustionFeature,
	                            KnockbackFeature knockbackFeature, TrackingFeature trackingFeature, CombatVersion version) {
		this.difficultyProvider = difficultyProvider;
		this.blockFeature = blockFeature;
		this.armorFeature = armorFeature;
		this.totemFeature = totemFeature;
		this.exhaustionFeature = exhaustionFeature;
		this.knockbackFeature = knockbackFeature;
		this.trackingFeature = trackingFeature;
		this.version = version;
	}
	
	@Override
	public void init(EventNode<Event> node) {
		node.addListener(EntityDamageEvent.class, this::handleDamage);
	}
	
	protected void handleDamage(EntityDamageEvent event) {
		// We will handle sound and animation ourselves
		event.setAnimation(false);
		SoundEvent sound = event.getSound();
		event.setSound(null);
		
		LivingEntity entity = event.getEntity();
		Damage damage = event.getDamage();
		Entity attacker = damage.getAttacker();
		
		DamageTypeInfo typeInfo = DamageTypeInfo.of(damage.getType());
		if (event.getEntity() instanceof Player player && typeInfo.shouldScaleWithDifficulty(damage))
			damage.setAmount(scaleWithDifficulty(player, damage.getAmount()));
		
		if (typeInfo.fire() && entity.hasEffect(PotionEffect.FIRE_RESISTANCE)) {
			event.setCancelled(true);
			return;
		}
		
		// This will be used to determine whether knockback should be applied
		// We can't just check if the remaining damage is 0 because this would apply no knockback for snowballs & eggs
		boolean fullyBlocked = false;
		if (blockFeature.isDamageBlocked(entity, damage)) {
			fullyBlocked = blockFeature.applyBlock(entity, damage);
		}
		
		float amount = damage.getAmount();
		
		if (typeInfo.freeze() && Objects.requireNonNull(MinecraftServer.getTagManager().getTag(
						net.minestom.server.gamedata.tags.Tag.BasicType.ENTITY_TYPES, "minecraft:freeze_hurts_extra_types"))
				.contains(entity.getEntityType().namespace())) {
			amount *= 5.0F;
		}
		
		if (typeInfo.damagesHelmet() && !entity.getEquipment(EquipmentSlot.HELMET).isAir()) {
			ItemUtils.damageArmor(entity, typeInfo, amount, EquipmentSlot.HELMET);
			amount *= 0.75F;
		}
		
		float amountBeforeProcessing = amount;
		
		// Invulnerability ticks
		boolean hurtSoundAndAnimation = true;
		long newDamageTime = entity.hasTag(DamageHandler.NEW_DAMAGE_TIME) ? entity.getTag(DamageHandler.NEW_DAMAGE_TIME) : -10000;
		if (entity.getAliveTicks() - newDamageTime < 0) {
			float lastDamage = entity.hasTag(DamageHandler.LAST_DAMAGE_AMOUNT) ? entity.getTag(DamageHandler.LAST_DAMAGE_AMOUNT) : 0;
			
			if (amount <= lastDamage) {
				event.setCancelled(true);
				return;
			}
			
			hurtSoundAndAnimation = false;
			amount = amount - lastDamage;
		}
		
		// Process armor and effects
		amount = armorFeature.getDamageWithProtection(entity, damage.getType(), amount);
		
		damage.setAmount(amount);
		FinalDamageEvent finalDamageEvent = new FinalDamageEvent(entity, damage, 10);
		EventDispatcher.call(finalDamageEvent);
		// New amount has been set in the Damage class
		amount = damage.getAmount();
		
		if (finalDamageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		
		// Register damage to tracking feature
		boolean register = version.legacy() || amount > 0;
		if (register && entity instanceof Player player)
			trackingFeature.recordDamage(player, attacker, damage);
		
		// Exhaustion from damage
		if (amountBeforeProcessing != 0 && entity instanceof Player player)
			exhaustionFeature.addDamageExhaustion(player, damage.getType());
		
		if (register) entity.setTag(LAST_DAMAGE_AMOUNT, amountBeforeProcessing);
		
		if (hurtSoundAndAnimation) {
			entity.setTag(NEW_DAMAGE_TIME, entity.getAliveTicks() + finalDamageEvent.getInvulnerabilityTicks());
			
			// Send damage animation
			entity.sendPacketToViewersAndSelf(new DamageEventPacket(
					entity.getEntityId(),
					damage.getType().id(),
					damage.getAttacker() == null ? 0 : damage.getAttacker().getEntityId() + 1,
					damage.getSource() == null ? 0 : damage.getSource().getEntityId() + 1,
					null
			));
			
			if (!fullyBlocked && damage.getType() != DamageType.DROWN) {
				if (attacker != null && !typeInfo.explosive()) {
					knockbackFeature.applyDamageKnockback(damage, entity);
				} else {
					// Update velocity
					//TODO does this even do anything?
					entity.setVelocity(entity.getVelocity());
				}
			}
		}
		
		if (fullyBlocked) {
			event.setCancelled(true);
			return;
		}
		
		boolean death = false;
		float totalHealth = entity.getHealth() +
				(entity instanceof Player player ? player.getAdditionalHearts() : 0);
		if (totalHealth - amount <= 0) {
			boolean totem = totemFeature.tryProtect(entity, damage.getType());
			
			if (totem) {
				event.setCancelled(true);
			} else {
				death = true;
				if (hurtSoundAndAnimation) {
					// Death sound
					sound = entity instanceof Player ? SoundEvent.ENTITY_PLAYER_DEATH : SoundEvent.ENTITY_GENERIC_DEATH;
				}
			}
		} else if (hurtSoundAndAnimation) {
			// Workaround to have different types make a different sound,
			// but only if the sound has not been changed by damage#getSound
			//TODO improve
			if (entity instanceof Player && sound == SoundEvent.ENTITY_PLAYER_HURT) {
				if (typeInfo.fire()) {
					sound = SoundEvent.ENTITY_PLAYER_HURT_ON_FIRE;
				} else if (typeInfo.thorns()) {
					sound = SoundEvent.ENCHANT_THORNS_HIT;
				} else if (damage.getType() == DamageType.DROWN) {
					sound = SoundEvent.ENTITY_PLAYER_HURT_DROWN;
				} else if (damage.getType() == DamageType.SWEET_BERRY_BUSH) {
					sound = SoundEvent.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH;
				} else if (damage.getType() == DamageType.FREEZE) {
					sound = SoundEvent.ENTITY_PLAYER_HURT_FREEZE;
				}
			}
		}
		
		// Play sound (copied from Minestom, because of complications with cancelling)
		if (sound != null) entity.sendPacketToViewersAndSelf(new SoundEffectPacket(
				sound, null, entity instanceof Player ? Sound.Source.PLAYER : Sound.Source.HOSTILE,
				entity.getPosition(),
				//TODO seed randomizing?
				1.0f, 1.0f, 0
		));
		
		if (death && !event.isCancelled()) {
			EntityPreDeathEvent entityPreDeathEvent = new EntityPreDeathEvent(entity, damage.getType());
			EventDispatcher.call(entityPreDeathEvent);
			if (entityPreDeathEvent.isCancelled()) event.setCancelled(true);
			if (entityPreDeathEvent.isCancelDeath()) amount = 0;
		}
		
		damage.setAmount(amount);
		
		// lastDamage field is set when event is not cancelled but should also when cancelled
		if (register) EntityUtils.setLastDamage(entity, damage);
		
		// The Minestom damage method should return false if there was no hurt animation,
		// because otherwise the attack feature will deal extra knockback
		if (!event.isCancelled() && !hurtSoundAndAnimation) {
			event.setCancelled(true);
			damageManually(entity, amount);
		}
	}
	
	protected float scaleWithDifficulty(Player player, float amount) {
		return switch (difficultyProvider.getValue(player)) {
			case PEACEFUL -> -1;
			case EASY -> Math.min(amount / 2.0f + 1.0f, amount);
			case HARD -> amount * 3.0f / 2.0f;
			default -> amount;
		};
	}
	
	private static void damageManually(LivingEntity entity, float damage) {
		// Additional hearts support
		if (entity instanceof Player player) {
			final float additionalHearts = player.getAdditionalHearts();
			if (additionalHearts > 0) {
				if (damage > additionalHearts) {
					damage -= additionalHearts;
					player.setAdditionalHearts(0);
				} else {
					player.setAdditionalHearts(additionalHearts - damage);
					damage = 0;
				}
			}
		}
		
		// Set the final entity health
		entity.setHealth(entity.getHealth() - damage);
	}
}

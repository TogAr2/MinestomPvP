package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.config.DamageConfig;
import io.github.bloepiloepi.pvp.config.PvPConfig;
import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.Tracker;
import io.github.bloepiloepi.pvp.events.*;
import io.github.bloepiloepi.pvp.legacy.LegacyKnockbackSettings;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.utils.DamageUtils;
import io.github.bloepiloepi.pvp.utils.ItemUtils;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.world.Difficulty;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class DamageListener {
	public static final net.minestom.server.tag.Tag<Long> NEW_DAMAGE_TIME = net.minestom.server.tag.Tag.Long("lastDamageTime");
	public static final net.minestom.server.tag.Tag<Float> LAST_DAMAGE_AMOUNT = net.minestom.server.tag.Tag.Float("lastDamageAmount");

	public static EventNode<EntityInstanceEvent> events(DamageConfig config) {
		EventNode<EntityInstanceEvent> node = EventNode.type("damage-events", PvPConfig.ENTITY_INSTANCE_FILTER);

		node.addListener(EventListener.builder(EntityDamageEvent.class)
				.handler(event -> handleEntityDamage(event, config))
				.build());

		if (config.isFallDamageEnabled()) {
			node.addListener(EntityTickEvent.class, event -> {
				if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
				if (livingEntity instanceof Player) return;
				var previousPosition = EntityUtils.getPreviousPosition(livingEntity);
				if (previousPosition == null) return;
				handleEntityFallDamage(livingEntity, previousPosition, livingEntity.getPosition(), livingEntity.isOnGround());
			});
			node.addListener(PlayerMoveEvent.class, event -> {
				var player = event.getPlayer();
				handleEntityFallDamage(player, player.getPosition(), event.getNewPosition(), event.isOnGround());
			});
		}

		return node;
	}

	private static void handleEntityFallDamage(LivingEntity livingEntity, Pos currentPosition, Pos newPosition, boolean isOnGround) {
		double dy = newPosition.y() - currentPosition.y();
		Double fallDistance = Tracker.fallDistance.getOrDefault(livingEntity.getUuid(), 0.0);

		if ((livingEntity instanceof Player player && player.isFlying()) || EntityUtils.hasEffect(livingEntity, PotionEffect.LEVITATION)
				|| EntityUtils.hasEffect(livingEntity, PotionEffect.SLOW_FALLING) || dy > 0) {
			Tracker.fallDistance.put(livingEntity.getUuid(), 0.0);
			return;
		}
		if (livingEntity.isFlyingWithElytra() && livingEntity.getVelocity().y() > -0.5) {
			Tracker.fallDistance.put(livingEntity.getUuid(), 1.0);
			return;
		}

		if (fallDistance > 3.0 && isOnGround) {
			Block block = Objects.requireNonNull(livingEntity.getInstance()).getBlock(getLandingPos(livingEntity, newPosition));
			if (!block.isAir()) {
				double damageDistance = Math.ceil(fallDistance - 3.0);
				double d = Math.min(0.2 + damageDistance / 15.0, 2.5);
				int particleCount = (int) (150 * d);

				livingEntity.sendPacketToViewersAndSelf(ParticleCreator.createParticlePacket(
						Particle.BLOCK,
						false,
						newPosition.x(), newPosition.y(), newPosition.z(),
						0, 0, 0,
						0.15f, particleCount,
						writer -> writer.writeVarInt(block.stateId())
				));
			}
		}

		if (isOnGround) {
			Tracker.fallDistance.put(livingEntity.getUuid(), 0.0);

			if (livingEntity instanceof Player player && !player.getGameMode().canTakeDamage()) return;
			int damage = getFallDamage(livingEntity, fallDistance);
			if (damage > 0) {
				if (livingEntity instanceof Player player) {
					SoundEvent sound = damage > 4 ? SoundEvent.ENTITY_PLAYER_BIG_FALL : SoundEvent.ENTITY_PLAYER_SMALL_FALL;
					SoundManager.sendToAround(player, player, sound, Sound.Source.PLAYER, 1.0f, 1.0f);
				} else {
					SoundEvent sound = damage > 4 ? SoundEvent.ENTITY_GENERIC_BIG_FALL : SoundEvent.ENTITY_GENERIC_SMALL_FALL;
					SoundManager.sendToAround(livingEntity, sound, Sound.Source.HOSTILE, 1.0f, 1.0f);
				}

				livingEntity.damage(CustomDamageType.FALL, damage);
			}
		} else if (dy < 0) {
			Tracker.fallDistance.put(livingEntity.getUuid(), fallDistance - dy);
		}
	}

	private static int getFallDamage(LivingEntity livingEntity, double fallDistance) {
		float reduce = EntityUtils.hasEffect(livingEntity, PotionEffect.JUMP_BOOST)
				? EntityUtils.getEffect(livingEntity, PotionEffect.JUMP_BOOST).amplifier() + 1
				: 0;
		return (int) Math.ceil(fallDistance - 3.0 - reduce);
	}

	private static Point getLandingPos(LivingEntity livingEntity, Pos position) {
		position = position.add(0, -0.2, 0);
		if (Objects.requireNonNull(livingEntity.getInstance()).getBlock(position).isAir()) {
			position = position.add(0, -1, 0);
			Block block = livingEntity.getInstance().getBlock(position);
			Tag fences = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:fences");
			Tag walls = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:walls");
			Tag fenceGates = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:fence_gates");
			assert fences != null;
			assert walls != null;
			assert fenceGates != null;
			if (fences.contains(block.namespace()) || walls.contains(block.namespace()) || fenceGates.contains(block.namespace())) {
				return position;
			}
		}

		return position;
	}

	public static void handleEntityDamage(EntityDamageEvent event, DamageConfig config) {
		event.setAnimation(false);
		event.setSound(null);

		float amount = event.getDamage();

		CustomDamageType type;
		if (event.getDamageType() instanceof CustomDamageType) {
			type = (CustomDamageType) event.getDamageType();
		} else {
			if (event.getDamageType() == DamageType.GRAVITY) {
				type = CustomDamageType.FALL;
			} else if (event.getDamageType() == DamageType.ON_FIRE) {
				type = CustomDamageType.ON_FIRE;
			} else {
				type = CustomDamageType.OUT_OF_WORLD;
			}
		}

		if (event.getEntity() instanceof Player && type.isScaledWithDifficulty()) {
			Difficulty difficulty = MinecraftServer.getDifficulty();
			switch (difficulty) {
				case PEACEFUL -> {
					event.setCancelled(true);
					return;
				}
				case EASY -> amount = Math.min(amount / 2.0F + 1.0F, amount);
				case HARD -> amount = amount * 3.0F / 2.0F;
			}
		}

		LivingEntity entity = event.getEntity();
		if (type.isFire() && EntityUtils.hasEffect(entity, PotionEffect.FIRE_RESISTANCE)) {
			event.setCancelled(true);
			return;
		}

		if (config.isEquipmentDamageEnabled() && type.damagesHelmet() && !entity.getEquipment(EquipmentSlot.HELMET).isAir()) {
			ItemUtils.damageArmor(entity, type, amount, EquipmentSlot.HELMET);
			amount *= 0.75F;
		}

		Entity attacker = type.getEntity();
		if (entity instanceof Player && attacker instanceof LivingEntity) {
			Tracker.lastDamagedBy.put(entity.getUuid(), (LivingEntity) attacker);
			Tracker.lastDamageTime.put(entity.getUuid(), System.currentTimeMillis());
		}

		boolean shield = false;
		if (config.isShieldEnabled() && amount > 0.0F && EntityUtils.blockedByShield(entity, type, config.isLegacyShieldMechanics())) {
			float resultingDamage = 0.0F;
			if (config.isLegacyShieldMechanics()) {
				resultingDamage = (amount + 1.0F) * 0.5F;
				if (resultingDamage < 0.0F)
					resultingDamage = 0.0F;
			}

			DamageBlockEvent damageBlockEvent = new DamageBlockEvent(entity, amount, resultingDamage);
			EventDispatcher.call(damageBlockEvent);

			if (!damageBlockEvent.isCancelled()) {
				if (config.isEquipmentDamageEnabled() && amount >= 3) {
					int shieldDamage = 1 + (int) Math.floor(amount);
					Player.Hand hand = EntityUtils.getActiveHand(entity);
					ItemUtils.damageEquipment(entity, hand == Player.Hand.MAIN ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, shieldDamage);

					if (entity.getItemInHand(hand).isAir()) {
						((LivingEntityMeta) entity.getEntityMeta()).setHandActive(false);
						SoundManager.sendToAround(
								entity instanceof Player player ? player : null, entity,
								SoundEvent.ITEM_SHIELD_BREAK, Sound.Source.PLAYER,
								0.8F, 0.8F + ThreadLocalRandom.current().nextFloat() * 0.4F
						);
					}
				}

				amount = damageBlockEvent.getResultingDamage();

				if (!config.isLegacyShieldMechanics()) {
					if (!type.isProjectile()) {
						if (attacker instanceof LivingEntity) {
							EntityUtils.takeShieldHit(entity, (LivingEntity) attacker, damageBlockEvent.knockbackAttacker());
						}
					}

					shield = true;
				}
			}
		}

		boolean hurtSoundAndAnimation = true;
		float amountBeforeProcessing = amount;
		long newDamageTime = entity.hasTag(NEW_DAMAGE_TIME) ? entity.getTag(NEW_DAMAGE_TIME) : -10000;
		if (entity.getAliveTicks() - newDamageTime > 0) {
			float lastDamage = entity.hasTag(LAST_DAMAGE_AMOUNT) ? entity.getTag(LAST_DAMAGE_AMOUNT) : 0;

			if (amount <= lastDamage) {
				event.setCancelled(true);
				return;
			}

			amount = applyDamage(entity, type, amount - lastDamage, config);
			hurtSoundAndAnimation = false;
		} else {
			amount = applyDamage(entity, type, amount, config);
		}

		FinalDamageEvent finalDamageEvent = new FinalDamageEvent(entity, type, amount, config.getInvulnerabilityTicks());
		EventDispatcher.call(finalDamageEvent);

		amount = finalDamageEvent.getDamage();

		boolean register = config.isLegacy() || finalDamageEvent.getDamage() > 0;
		if (register && entity instanceof Player)
			Tracker.combatManager.get(entity.getUuid()).recordDamage(type, amount);

		if (!register || finalDamageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		entity.setTag(LAST_DAMAGE_AMOUNT, amountBeforeProcessing);

		if (hurtSoundAndAnimation) {
			entity.setTag(NEW_DAMAGE_TIME, entity.getAliveTicks() + finalDamageEvent.getInvulnerabilityTicks());

			if (shield) {
				entity.triggerStatus((byte) 29);
			} else if (type instanceof CustomEntityDamage && ((CustomEntityDamage) type).isThorns()) {
				if (config.isDamageAnimation()) entity.triggerStatus((byte) 33);
			} else {
				byte status;
				if (type == CustomDamageType.DROWN) {
					//Drown sound and animation
					status = 36;
				} else if (type.isFire()) {
					//Burn sound and animation
					status = 37;
				} else if (type == CustomDamageType.SWEET_BERRY_BUSH) {
					//Sweet berry bush sound and animation
					status = 44;
				} else if (type == CustomDamageType.FREEZE) {
					//Freeze sound and animation
					status = 57;
				} else {
					//Damage sound and animation
					status = 2;
				}

				if (config.isDamageAnimation()) entity.triggerStatus(status);
			}

			if (attacker != null && !shield) {
				double dx = attacker.getPosition().x() - entity.getPosition().x();
				double dz = attacker.getPosition().z() - entity.getPosition().z();

				ThreadLocalRandom random = ThreadLocalRandom.current();
				for(; dx * dx + dz * dz < 0.0001; dz = random.nextDouble(-1, 1) * 0.01) {
					dx = random.nextDouble(-1, 1) * 0.01;
				}

				Entity directAttacker = type.getDirectEntity();
				if (directAttacker == null) {
					directAttacker = attacker;
				}
				double finalDx = dx;
				double finalI = dz;
				if (!config.isLegacyKnockback()) {
					EntityKnockbackEvent entityKnockbackEvent = new EntityKnockbackEvent(entity, directAttacker, false, false, 0.4F);
					EventDispatcher.callCancellable(entityKnockbackEvent, () -> {
						float strength = entityKnockbackEvent.getStrength();
						entity.takeKnockback(strength, finalDx, finalI);
					});
				} else {
					double magnitude = Math.sqrt(dx * dx + dz * dz);
					LegacyKnockbackEvent legacyKnockbackEvent = new LegacyKnockbackEvent(entity, directAttacker, false);
					EventDispatcher.callCancellable(legacyKnockbackEvent, () -> {
						LegacyKnockbackSettings settings = legacyKnockbackEvent.getSettings();
						Vec newVelocity = entity.getVelocity();

						double horizontal = settings.horizontal();
						newVelocity = newVelocity.withX((newVelocity.x() / 2) - (finalDx / magnitude * horizontal));
						newVelocity = newVelocity.withY((newVelocity.y() / 2) + settings.vertical());
						newVelocity = newVelocity.withZ((newVelocity.z() / 2) - (finalI / magnitude * horizontal));

						if (newVelocity.y() > settings.verticalLimit())
							newVelocity = newVelocity.withY(settings.verticalLimit());

						entity.setVelocity(newVelocity);
					});
				}
			} else if (type != CustomDamageType.DROWN && (!shield || amount > 0)) {
				// Update velocity
				entity.setVelocity(entity.getVelocity());
			}
		}

		if (shield) {
			event.setCancelled(true);
			return;
		}

		SoundEvent sound = null;

		boolean death = false;
		float totalHealth = entity.getHealth() + (entity instanceof Player ? ((Player) entity).getAdditionalHearts() : 0);
		if (totalHealth - amount <= 0) {
			boolean totem = totemProtection(entity, type);

			if (totem) {
				event.setCancelled(true);
			} else {
				death = true;
				if (hurtSoundAndAnimation) {
					//Death sound
					sound = type.getDeathSound(entity);
				}
			}
		} else if (hurtSoundAndAnimation) {
			//Damage sound
			sound = type.getSound(entity);
		}

		//Play sound
		if (config.isSoundsEnabled() && sound != null) {
			Sound.Source soundCategory;
			if (entity instanceof Player) {
				soundCategory = Sound.Source.PLAYER;
			} else {
				// TODO: separate living entity categories
				soundCategory = Sound.Source.HOSTILE;
			}

			SoundEffectPacket damageSoundPacket = new SoundEffectPacket(
					sound, soundCategory,
					entity.getPosition(),
					1.0f, 1.0f
			);
			entity.sendPacketToViewersAndSelf(damageSoundPacket);
		}

		if (death && !event.isCancelled()) {
			EntityPreDeathEvent entityPreDeathEvent = new EntityPreDeathEvent(entity, type);
			EventDispatcher.call(entityPreDeathEvent);
			if (entityPreDeathEvent.isCancelled()) {
				event.setCancelled(true);
			}
			if (entityPreDeathEvent.isCancelDeath()) {
				amount = 0;
			}
		}

		if (config.shouldPerformDamage()) {
			// The Minestom damage method should return false if there was no hurt animation,
			// because otherwise the AttackManager will deal extra knockback
			if (!event.isCancelled() && !hurtSoundAndAnimation) {
				event.setCancelled(true);
				damageManually(entity, amount);
			} else {
				event.setDamage(amount);
			}
		} else {
			event.setCancelled(true);
		}
	}

	public static boolean totemProtection(LivingEntity entity, CustomDamageType type) {
		if (type.isOutOfWorld()) return false;

		boolean hasTotem = false;

		for (Player.Hand hand : Player.Hand.values()) {
			ItemStack stack = entity.getItemInHand(hand);
			if (stack.material() == Material.TOTEM_OF_UNDYING) {
				TotemUseEvent totemUseEvent = new TotemUseEvent(entity, hand);
				EventDispatcher.call(totemUseEvent);

				if (totemUseEvent.isCancelled()) continue;

				hasTotem = true;
				entity.setItemInHand(hand, stack.withAmount(stack.amount() - 1));
				break;
			}
		}

		if (hasTotem) {
			entity.setHealth(1.0F);
			entity.clearEffects();
			entity.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, 900, PotionListener.defaultFlags()));
			entity.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 1, 100, PotionListener.defaultFlags()));
			entity.addEffect(new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 800, PotionListener.defaultFlags()));

			//Totem particles
			entity.triggerStatus((byte) 35);
		}

		return hasTotem;
	}

	public static float applyDamage(LivingEntity entity, CustomDamageType type, float amount, DamageConfig config) {
		amount = applyArmorToDamage(entity, type, amount, config);
		amount = applyEnchantmentsToDamage(entity, type, amount, config);

		if (config.isExhaustionEnabled() && amount != 0.0F && entity instanceof Player) {
			EntityUtils.addExhaustion((Player) entity, type.getExhaustion() * (config.isLegacy() ? 3 : 1));
		}

		return amount;
	}

	public static float applyArmorToDamage(LivingEntity entity, CustomDamageType type, float amount, DamageConfig config) {
		if (config.isArmorDisabled()) return amount;

		if (!type.bypassesArmor()) {
			float armorValue = entity.getAttributeValue(Attribute.ARMOR);
			if (!config.isLegacy()) {
				amount = DamageUtils.getDamageLeft(amount, (float) Math.floor(armorValue), entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS));
			} else {
				int i = 25 - (int) armorValue;
				float f1 = amount * (float) i;
				amount = f1 / 25;
			}
		}

		return amount;
	}

	public static float applyEnchantmentsToDamage(LivingEntity entity, CustomDamageType type, float amount, DamageConfig config) {
		if (type.isUnblockable()) {
			return amount;
		} else {
			int k;
			if (EntityUtils.hasEffect(entity, PotionEffect.RESISTANCE)) {
				k = (EntityUtils.getEffect(entity, PotionEffect.RESISTANCE).amplifier() + 1) * 5;
				int j = 25 - k;
				float f = amount * (float) j;
				amount = Math.max(f / 25, 0);
			}

			if (config.isArmorDisabled()) return amount;

			if (amount <= 0) {
				return 0;
			} else {
				k = EnchantmentUtils.getProtectionAmount(EntityUtils.getArmorItems(entity), type);
				if (!config.isLegacy()) {
					if (k > 0) {
						amount = DamageUtils.getInflictedDamage(amount, (float) k);
					}
				} else {
					if (k > 20) {
						k = 20;
					}

					if (k > 0) {
						int j = 25 - k;
						float f = amount * (float) j;
						amount = f / 25;
					}
				}

				return amount;
			}
		}
	}

	public static void damageManually(LivingEntity entity, float damage) {
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

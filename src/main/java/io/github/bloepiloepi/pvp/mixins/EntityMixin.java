package io.github.bloepiloepi.pvp.mixins;

import io.github.bloepiloepi.pvp.entities.Tracker;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.monster.PiglinMeta;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.player.PlayerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public abstract @NotNull UUID getUuid();
	@Shadow protected Pos position;
	@Shadow protected EntityType entityType;
	@Shadow private BoundingBox boundingBox;
	
	@Shadow @NotNull public abstract Entity.@NotNull Pose getPose();
	@Shadow @NotNull public abstract EntityMeta getEntityMeta();
	@Shadow public abstract @Nullable Instance getInstance();
	@Shadow public abstract boolean isOnFire();
	@Shadow public abstract void setOnFire(boolean fire);
	
	@Shadow public abstract void setVelocity(@NotNull Vec velocity);
	
	@Shadow protected Vec velocity;
	@Shadow protected boolean onGround;
	private double eyeHeight;
	
	/**
	 * This knockback formula accurately represents vanilla knockback, which does apply to players
	 * The issue is that it doesn't apply to Minestom entities,
	 * probably because their physics are handled differently
	 */
	@Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
	public void takeKnockback(final float strength, final double x, final double z, CallbackInfo ci) {
		// Only use this formula if the entity is a remote player
		if (PlayerUtils.isSocketClient((Entity) (Object) this)) {
			if (strength > 0) {
				float strengthFactor = strength * MinecraftServer.TICK_PER_SECOND;
				final Vec velocityModifier = new Vec(x, z)
						.normalize()
						.mul(strengthFactor);
				setVelocity(new Vec(velocity.x() / 2d - velocityModifier.x(),
						onGround ? Math.min(.4d * MinecraftServer.TICK_PER_SECOND, velocity.y() / 2d + strengthFactor) : velocity.y(),
						velocity.z() / 2d - velocityModifier.z()
				));
			}
			
			ci.cancel();
		}
	}
	
	@Inject(method = "<init>(Lnet/minestom/server/entity/EntityType;Ljava/util/UUID;)V", at = @At("TAIL"))
	private void onInit(@NotNull EntityType entityType, @NotNull UUID uuid, CallbackInfo ci) {
		eyeHeight = getNewEyeHeight(getPose());
	}
	
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "teleport(Lnet/minestom/server/coordinate/Pos;[J)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "HEAD"))
	private void onTeleport(@NotNull Pos position, long[] chunks, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		if ((Object) this instanceof Player) {
			if (Tracker.spectating.get(getUuid()) != (Object) this) {
				if (this.position.distance(position) < 16) return;
				
				((Player) (Object) this).stopSpectating();
			}
		}
	}
	
	@Inject(method = "setPose", at = @At("TAIL"))
	private void onSetPose(Entity.Pose pose, CallbackInfo ci) {
		eyeHeight = getNewEyeHeight(pose);
	}
	
	/**
	 * @author me
	 * @return the eye height
	 */
	@Overwrite
	public double getEyeHeight() {
		return eyeHeight;
	}
	
	@SuppressWarnings("ConstantConditions")
	private double getNewEyeHeight(Entity.Pose pose) {
		if ((Object) this instanceof LivingEntity) {
			return pose == Entity.Pose.SLEEPING ? 0.2F : getStandingEyeHeight(pose);
		} else {
			if (entityType == EntityType.ARROW || entityType == EntityType.SPECTRAL_ARROW) {
				return 0.13;
			} else if (entityType == EntityType.BOAT) {
				return boundingBox.getHeight();
			} else if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.GLOW_ITEM_FRAME) {
				return 0.0;
			} else if (entityType == EntityType.LEASH_KNOT) {
				return 0.0625;
			} else if (entityType == EntityType.TNT) {
				return 0.15;
			}
			
			return boundingBox.getHeight() * 0.85;
		}
	}
	
	private double getStandingEyeHeight(Entity.Pose pose) {
		if (entityType == EntityType.COD || entityType == EntityType.SALMON || entityType == EntityType.TROPICAL_FISH || entityType == EntityType.PUFFERFISH) {
			return boundingBox.getHeight() * 0.65;
		} else if (entityType == EntityType.DONKEY || entityType == EntityType.HORSE || entityType == EntityType.LLAMA || entityType == EntityType.MULE || entityType == EntityType.SKELETON_HORSE || entityType == EntityType.TRADER_LLAMA || entityType == EntityType.ZOMBIE_HORSE || entityType == EntityType.SHEEP) {
			return boundingBox.getHeight() * 0.95;
		} else if (entityType == EntityType.WITHER_SKELETON) {
			return 2.1;
		} else if (entityType == EntityType.SKELETON || entityType == EntityType.STRAY) {
			return 1.74;
		} else if (entityType == EntityType.VILLAGER) {
			return ((AgeableMobMeta) getEntityMeta()).isBaby() ? 0.81 : 1.62;
		} else if (entityType == EntityType.ARMOR_STAND) {
			return boundingBox.getHeight() * (((ArmorStandMeta) getEntityMeta()).isSmall() ? 0.5 : 0.9);
		} else if (entityType == EntityType.AXOLOTL) {
			return boundingBox.getHeight() * 0.655;
		} else if (entityType == EntityType.BAT || entityType == EntityType.SQUID || entityType == EntityType.GUARDIAN || entityType == EntityType.ELDER_GUARDIAN || entityType == EntityType.CAT || entityType == EntityType.BEE) {
			return boundingBox.getHeight() / 2;
		} else if (entityType == EntityType.CAVE_SPIDER) {
			return 0.45;
		} else if (entityType == EntityType.CHICKEN) {
			return boundingBox.getHeight() * (((AgeableMobMeta) getEntityMeta()).isBaby() ? 0.85 : 0.92);
		} else if (entityType == EntityType.COW) {
			return ((AgeableMobMeta) getEntityMeta()).isBaby() ? boundingBox.getHeight() * 0.95 : 1.3;
		} else if (entityType == EntityType.DOLPHIN) {
			return 0.3;
		} else if (entityType == EntityType.ENDERMAN) {
			return 2.55;
		} else if (entityType == EntityType.ENDERMITE || entityType == EntityType.SILVERFISH) {
			return 0.13;
		} else if (entityType == EntityType.FOX) {
			return ((AgeableMobMeta) getEntityMeta()).isBaby() ? boundingBox.getHeight() * 0.85 : 0.4;
		} else if (entityType == EntityType.GHAST) {
			return 2.6;
		} else if (entityType == EntityType.GIANT) {
			return 10.440001;
		} else if (entityType == EntityType.PARROT) {
			return boundingBox.getHeight() * 0.6;
		} else if (entityType == EntityType.PHANTOM) {
			return boundingBox.getHeight() * 0.35;
		} else if (entityType == EntityType.PIGLIN) {
			return ((PiglinMeta) getEntityMeta()).isBaby() ? 0.93 : 1.74;
		} else if (entityType == EntityType.PLAYER) {
			switch (pose) {
				case FALL_FLYING:
				case SWIMMING:
				case SPIN_ATTACK:
					return 0.4;
				case SNEAKING:
					return 1.27;
				default:
					return 1.62;
			}
		} else if (entityType == EntityType.SHULKER) {
			return 0.5;
		} else if (entityType == EntityType.SLIME) {
			return boundingBox.getHeight() * 0.625;
		} else if (entityType == EntityType.SNOW_GOLEM) {
			return 1.7;
		} else if (entityType == EntityType.SPIDER) {
			return 0.65;
		} else if (entityType == EntityType.WITCH) {
			return 1.62;
		} else if (entityType == EntityType.WOLF) {
			return boundingBox.getHeight() * 0.8;
		} else if (entityType == EntityType.ZOMBIE) {
			return ((ZombieMeta) getEntityMeta()).isBaby() ? 0.93 : 1.74;
		}
		
		return boundingBox.getHeight() * 0.85;
	}
}

package io.github.bloepiloepi.pvp.mixins;

import io.github.bloepiloepi.pvp.entities.Tracker;
import net.minestom.server.collision.BoundingBox;
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
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
	
	@Shadow public abstract @NotNull UUID getUuid();
	@Shadow @Final protected Position position;
	@Shadow protected EntityType entityType;
	@Shadow private BoundingBox boundingBox;
	
	@Shadow @NotNull public abstract Entity.@NotNull Pose getPose();
	@Shadow @NotNull public abstract EntityMeta getEntityMeta();
	@Shadow public abstract @Nullable Instance getInstance();
	@Shadow public abstract boolean isOnFire();
	@Shadow public abstract void setOnFire(boolean fire);
	
	private double eyeHeight;
	
	@Inject(method = "<init>(Lnet/minestom/server/entity/EntityType;Ljava/util/UUID;)V", at = @At("TAIL"))
	private void onInit(@NotNull EntityType entityType, @NotNull UUID uuid, CallbackInfo ci) {
		eyeHeight = getNewEyeHeight(getPose());
	}
	
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "teleport(Lnet/minestom/server/utils/Position;[JLjava/lang/Runnable;)V", at = @At(value = "HEAD"))
	private void onTeleport(@NotNull Position position, long[] chunks, @Nullable Runnable callback, CallbackInfo ci) {
		if ((Object) this instanceof Player) {
			if (Tracker.spectating.get(getUuid()) != (Object) this) {
				if (this.position.getDistance(position) < 16) return;
				
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
	 */
	@Overwrite
	public double getEyeHeight() {
		return eyeHeight;
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(long time, CallbackInfo ci) {
		if (getInstance() == null) return;
		
		if (isOnFire() && Tracker.fireExtinguishTime.containsKey(getUuid())) {
			if (time > Tracker.fireExtinguishTime.get(getUuid())) {
				setOnFire(false);
			}
		}
	}
	
	@Inject(method = "remove", at = @At("TAIL"))
	private void onRemove(CallbackInfo ci) {
		Tracker.fireExtinguishTime.remove(getUuid());
	}
	
	@SuppressWarnings("ConstantConditions")
	private double getNewEyeHeight(Entity.Pose pose) {
		if ((Object) this instanceof LivingEntity) {
			return pose == Entity.Pose.SLEEPING ? 0.2F : getStandingEyeHeight(pose);
		} else {
			switch (entityType) {
				case ARROW:
				case SPECTRAL_ARROW:
					return 0.13;
				case BOAT:
					return boundingBox.getHeight();
				case ITEM_FRAME:
				case GLOW_ITEM_FRAME:
					return 0.0;
				case LEASH_KNOT:
					return 0.0625;
				case TNT:
					return 0.15;
				default:
					return boundingBox.getHeight() * 0.85;
			}
		}
	}
	
	private double getStandingEyeHeight(Entity.Pose pose) {
		switch (entityType) {
			case COD:
			case SALMON:
			case TROPICAL_FISH:
			case PUFFERFISH:
				return boundingBox.getHeight() * 0.65;
			case DONKEY:
			case HORSE:
			case LLAMA:
			case MULE:
			case SKELETON_HORSE:
			case TRADER_LLAMA:
			case ZOMBIE_HORSE:
			case SHEEP:
				return boundingBox.getHeight() * 0.95;
			case WITHER_SKELETON:
				return 2.1;
			case SKELETON:
			case STRAY:
				return 1.74;
			case VILLAGER:
				return ((AgeableMobMeta) getEntityMeta()).isBaby() ? 0.81 : 1.62;
			case ARMOR_STAND:
				return boundingBox.getHeight() * (((ArmorStandMeta) getEntityMeta()).isSmall() ? 0.5 : 0.9);
			case AXOLOTL:
				return boundingBox.getHeight() * 0.655;
			case BAT:
			case SQUID:
			case GUARDIAN:
			case ELDER_GUARDIAN:
			case CAT:
			case BEE:
				return boundingBox.getHeight() / 2;
			case CAVE_SPIDER:
				return 0.45;
			case CHICKEN:
				return boundingBox.getHeight() * (((AgeableMobMeta) getEntityMeta()).isBaby() ? 0.85 : 0.92);
			case COW:
				return ((AgeableMobMeta) getEntityMeta()).isBaby() ? boundingBox.getHeight() * 0.95 : 1.3;
			case DOLPHIN:
				return 0.3;
			case ENDERMAN:
				return 2.55;
			case ENDERMITE:
			case SILVERFISH:
				return 0.13;
			case FOX:
				return ((AgeableMobMeta) getEntityMeta()).isBaby() ? boundingBox.getHeight() * 0.85 : 0.4;
			case GHAST:
				return 2.6;
			case GIANT:
				return 10.440001;
			case PARROT:
				return boundingBox.getHeight() * 0.6;
			case PHANTOM:
				return boundingBox.getHeight() * 0.35;
			case PIGLIN:
				return ((PiglinMeta) getEntityMeta()).isBaby() ? 0.93 : 1.74;
			case PLAYER:
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
			case SHULKER:
				return 0.5;
			case SLIME:
				return boundingBox.getHeight() * 0.625;
			case SNOW_GOLEM:
				return 1.7;
			case SPIDER:
				return 0.65;
			case WITCH:
				return 1.62;
			case WOLF:
				return boundingBox.getHeight() * 0.8;
			case ZOMBIE:
				return ((ZombieMeta) getEntityMeta()).isBaby() ? 0.93 : 1.74;
			default:
				return boundingBox.getHeight() * 0.85;
		}
	}
}

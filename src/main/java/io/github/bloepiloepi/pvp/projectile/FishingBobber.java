package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FishingBobber extends CustomEntityProjectile {
	public static final Map<UUID, FishingBobber> fishingBobbers = new ConcurrentHashMap<>();
	
	private final boolean legacy;
	private int stuckTime;
	private Entity hooked;
	private State state = State.IN_AIR;
	private Pos prevPos = Pos.ZERO;
	
	public FishingBobber(@Nullable Entity shooter, boolean legacy) {
		super(shooter, EntityType.FISHING_BOBBER, false);
		this.legacy = legacy;
		setOwnerEntity(shooter);
		
		if (legacy) setGravity(getGravityDragPerTick(), 0.04);
	}
	
	@Override
	public void tick(long time) {
		prevPos = getPosition();
		super.tick(time);
	}
	
	@Override
	public void update(long time) {
		if (!(getShooter() instanceof Player shooter)) {
			remove();
			return;
		}
		if (shouldStopFishing(shooter)) return;
		
		if (onGround) {
			stuckTime++;
			if (stuckTime >= 1200) {
				remove();
				return;
			}
		} else {
			stuckTime = 0;
		}
		
		if (state == State.IN_AIR) {
			if (hooked != null) {
				velocity = Vec.ZERO;
				setNoGravity(true);
				state = State.HOOKED_ENTITY;
			}
		} else {
			if (state == State.HOOKED_ENTITY) {
				if (hooked != null) {
					if (hooked.isRemoved() || hooked.getInstance() != getInstance()) {
						setHookedEntity(null);
						setNoGravity(false);
						state = State.IN_AIR;
					} else {
						Pos hookedPos = hooked.getPosition();
						teleport(hookedPos.withY(EntityUtils.getBodyY(hooked, 0.8)));
					}
				}
			}
		}
		
		//EntityUtils.updateProjectileRotation(this);
	}
	
	@Override
	public void onHit(Entity entity) {
		if (hooked != null) return;
		setHookedEntity(entity);
		
		if (legacy) {
			if (entity instanceof Player player
					&& (player == getShooter() || player.getGameMode() == GameMode.CREATIVE))
				return;
			
			Pos posNow = this.position;
			this.position = prevPos;
			if (EntityUtils.damage(entity, CustomDamageType.GENERIC, 0)) {
				entity.setVelocity(calculateLegacyKnockback(entity.getVelocity(), entity.getPosition()));
			}
			this.position = posNow;
		}
	}
	
	private void setHookedEntity(@Nullable Entity entity) {
		this.hooked = entity;
		((FishingHookMeta) getEntityMeta()).setHookedEntity(entity);
	}
	
	private void setOwnerEntity(@Nullable Entity entity) {
		((FishingHookMeta) getEntityMeta()).setOwnerEntity(entity);
	}
	
	private boolean shouldStopFishing(Player player) {
		boolean main = player.getItemInMainHand().material() == Material.FISHING_ROD;
		boolean off = player.getItemInOffHand().material() == Material.FISHING_ROD;
		if (player.isRemoved() || player.isDead() || (!main && !off)
				|| (!legacy && getDistanceSquared(player) > 1024)) {
			setOwnerEntity(null);
			remove();
			return true;
		}
		
		return false;
	}
	
	public int retrieve() {
		if (!(getShooter() instanceof Player shooter)) return 0;
		if (shouldStopFishing(shooter)) return 0;
		
		int durability = 0;
		if (hooked != null) {
			if (!legacy) {
				pullEntity(hooked);
				triggerStatus((byte) 31);
			}
			durability = hooked instanceof ItemEntity ? 3 : 5;
		}
		
		remove();
		
		return durability;
	}
	
	private void pullEntity(Entity entity) {
		Entity shooter = getShooter();
		if (shooter == null) return;
		
		Pos shooterPos = shooter.getPosition();
		Pos pos = getPosition();
		Vec velocity = new Vec(shooterPos.x() - pos.x(), shooterPos.y() - pos.y(),
				shooterPos.z() - pos.z()).mul(0.1);
		velocity = velocity.mul(MinecraftServer.TICK_PER_SECOND);
		entity.setVelocity(entity.getVelocity().add(velocity));
	}
	
	private Vec calculateLegacyKnockback(Vec currentVelocity, Pos entityPos) {
		Pos position = getPosition();
		double dx = position.x() - entityPos.x();
		double dz = position.z() - entityPos.z();
		
		while (dx * dx + dz * dz < 0.0001) {
			dx = (Math.random() - Math.random()) * 0.01;
			dz = (Math.random() - Math.random()) * 0.01;
		}
		
		double distance = Math.sqrt(dx * dx + dz * dz);
		
		double x = currentVelocity.x() / 2;
		double y = currentVelocity.y() / 2;
		double z = currentVelocity.z() / 2;
		
		// Normalize to have similar knockback on every distance
		x -= dx / distance * 0.4;
		y += 0.4;
		z -= dz / distance * 0.4;
		
		if (y > 0.4)
			y = 0.4;
		
		return new Vec(x, y, z).mul(MinecraftServer.TICK_PER_SECOND * 0.8);
	}
	
	@Override
	public void remove() {
		Entity shooter = getShooter();
		if (shooter != null) {
			if (fishingBobbers.get(shooter.getUuid()) == this) {
				fishingBobbers.remove(getShooter().getUuid());
			}
		}
		
		super.remove();
	}
	
	private enum State {
		IN_AIR,
		HOOKED_ENTITY,
		BOBBING
	}
}

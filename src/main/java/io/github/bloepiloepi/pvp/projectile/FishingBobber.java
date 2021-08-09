package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.entities.EntityUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FishingBobber extends EntityHittableProjectile {
	public static final Map<UUID, FishingBobber> fishingBobbers = new ConcurrentHashMap<>();
	
	private int stuckTime;
	private Entity hooked;
	private State state = State.IN_AIR;
	
	public FishingBobber(@Nullable Entity shooter) {
		super(shooter, EntityType.FISHING_BOBBER);
		setOwnerEntity(shooter);
	}
	
	@Override
	public void update(long time) {
		if (!(getShooter() instanceof Player)) {
			remove();
			return;
		}
		Player shooter = (Player) getShooter();
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
				getVelocity().zero();
				setNoGravity(true);
				state = State.HOOKED_ENTITY;
				return;
			}
		} else {
			if (state == State.HOOKED_ENTITY) {
				if (hooked != null) {
					if (hooked.isRemoved() || hooked.getInstance() != getInstance()) {
						setHookedEntity(null);
						setNoGravity(false);
						state = State.IN_AIR;
					} else {
						Position hookedPos = hooked.getPosition();
						teleport(new Position(hookedPos.getX(), EntityUtils.getBodyY(hooked, 0.8),
								hookedPos.getZ()));
					}
				}
				
				return;
			}
		}
		
		EntityUtils.updateProjectileRotation(this);
	}
	
	@Override
	protected boolean onHit(@Nullable Entity entity) {
		if (entity != null) {
			setHookedEntity(entity);
		}
		
		return false;
	}
	
	@Override
	public void onStuck() {}
	
	private void setHookedEntity(@Nullable Entity entity) {
		this.hooked = entity;
		((FishingHookMeta) getEntityMeta()).setHookedEntity(entity);
	}
	
	private void setOwnerEntity(@Nullable Entity entity) {
		((FishingHookMeta) getEntityMeta()).setOwnerEntity(entity);
	}
	
	private boolean shouldStopFishing(Player player) {
		boolean main = player.getItemInMainHand().getMaterial() == Material.FISHING_ROD;
		boolean off = player.getItemInOffHand().getMaterial() == Material.FISHING_ROD;
		if (player.isRemoved() || player.isDead() || (!main && !off) || getDistanceSquared(player) > 1024) {
			setOwnerEntity(null);
			remove();
			return true;
		}
		
		return false;
	}
	
	public int retrieve() {
		if (!(getShooter() instanceof Player)) return 0;
		Player shooter = (Player) getShooter();
		if (shouldStopFishing(shooter)) return 0;
		
		int durability = 0;
		if (hooked != null) {
			pullEntity(hooked);
			triggerStatus((byte) 31);
			durability = hooked instanceof ItemEntity ? 3 : 5;
		}
		
		remove();
		
		return durability;
	}
	
	private void pullEntity(Entity entity) {
		Entity shooter = getShooter();
		if (shooter == null) return;
		
		Position shooterPos = shooter.getPosition();
		Position pos = getPosition();
		Vector velocity = new Vector(shooterPos.getX() - pos.getX(), shooterPos.getY() - pos.getY(),
				shooterPos.getZ() - pos.getZ()).multiply(0.1);
		velocity.multiply(MinecraftServer.TICK_PER_SECOND);
		entity.setVelocity(EntityUtils.getActualVelocity(entity).add(velocity));
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

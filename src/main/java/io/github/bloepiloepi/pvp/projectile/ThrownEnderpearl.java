package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.Tracker;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ThrownEnderPearlMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class ThrownEnderpearl extends CustomEntityProjectile implements ItemHoldingProjectile {
	private Pos prevPos = Pos.ZERO;
	
	public ThrownEnderpearl(@Nullable Entity shooter) {
		super(shooter, EntityType.ENDER_PEARL, false);
	}
	
	private void teleportOwner() {
		Pos position = prevPos;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		for (int i = 0; i < 32; i++) {
			ParticlePacket packet = ParticleCreator.createParticlePacket(
					Particle.PORTAL, false,
					position.x(), position.y() + random.nextDouble() * 2, position.z(),
					(float) random.nextGaussian(), 0.0F, (float) random.nextGaussian(),
					0, 1, (writer) -> {});
			
			sendPacketToViewersAndSelf(packet);
		}
		
		if (isRemoved()) return;
		
		Entity shooter = getShooter();
		if (shooter != null) {
			Pos shooterPos = shooter.getPosition();
			position = position.withPitch(shooterPos.pitch()).withYaw(shooterPos.yaw());
		}
		
		if (shooter instanceof Player player) {
			if (player.isOnline() && player.getInstance() == getInstance()
					&& player.getEntityMeta().getBedInWhichSleepingPosition() == null) {
				if (player.getVehicle() != null) {
					player.getVehicle().removePassenger(player);
				}
				
				player.teleport(position);
				player.setTag(Tracker.FALL_DISTANCE, 0.0);
				
				player.damage(CustomDamageType.ENDER_PEARL, 5.0F);
			}
		} else if (shooter != null) {
			shooter.teleport(position);
		}
	}
	
	@Override
	public void onHit(Entity entity) {
		EntityUtils.damage(entity, CustomDamageType.thrown(this, getShooter()), 0.0F);
		
		teleportOwner();
		remove();
	}
	
	@Override
	public void onStuck() {
		teleportOwner();
		remove();
	}
	
	@Override
	public void tick(long time) {
		Entity shooter = getShooter();
		if (shooter instanceof Player && ((Player) shooter).isDead()) {
			remove();
		} else {
			prevPos = getPosition();
			super.tick(time);
		}
	}
	
	@Override
	public void setItem(@NotNull ItemStack item) {
		((ThrownEnderPearlMeta) getEntityMeta()).setItem(item);
	}
}

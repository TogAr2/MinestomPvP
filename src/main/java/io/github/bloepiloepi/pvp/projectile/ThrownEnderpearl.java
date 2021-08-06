package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ThrownEnderPearlMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class ThrownEnderpearl extends EntityHittableProjectile {
	
	public ThrownEnderpearl(@Nullable Entity shooter) {
		super(shooter, EntityType.ENDER_PEARL);
	}
	
	@Override
	public boolean onHit(@Nullable Entity entity) {
		if (entity != null) {
			EntityUtils.damage(entity, CustomDamageType.thrown(this, getShooter()), 0.0F);
		}
		
		Position position = getPosition();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		for (int i = 0; i < 32; i++) {
			ParticlePacket packet = ParticleCreator.createParticlePacket(
					Particle.PORTAL, false,
					position.getX(), position.getY() + random.nextDouble() * 2, position.getZ(),
					(float) random.nextGaussian(), 0.0F, (float) random.nextGaussian(),
					0, 1, (writer) -> {});
			
			sendPacketToViewersAndSelf(packet);
		}
		
		if (isRemoved()) return false;
		
		Entity shooter = getShooter();
		if (shooter != null) {
			position.setPitch(shooter.getPosition().getPitch());
			position.setYaw(shooter.getPosition().getYaw());
		}
		
		if (shooter instanceof Player) {
			Player player = (Player) shooter;
			
			if (player.isOnline() && player.getInstance() == getInstance()
					&& player.getEntityMeta().getBedInWhichSleepingPosition() == null) {
				if (player.getVehicle() != null) {
					player.getVehicle().removePassenger(player);
				}
				
				player.teleport(position);
				//TODO set falldistance to 0
				
				player.damage(CustomDamageType.FALL, 5.0F);
			}
		} else if (shooter != null) {
			shooter.teleport(position);
			//TODO set falldistance to 0
		}
		
		return true;
	}
	
	@Override
	public void tick(long time) {
		Entity shooter = getShooter();
		if (shooter instanceof Player && ((Player) shooter).isDead()) {
			remove();
		} else {
			super.tick(time);
		}
	}
	
	@Override
	public void setItem(@NotNull ItemStack item) {
		((ThrownEnderPearlMeta) getEntityMeta()).setItem(item);
	}
}

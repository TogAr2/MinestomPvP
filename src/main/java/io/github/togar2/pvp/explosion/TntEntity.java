package io.github.togar2.pvp.explosion;

import io.github.togar2.pvp.entity.EntityUtils;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.util.concurrent.ThreadLocalRandom;

public class TntEntity extends Entity {
	private final LivingEntity causingEntity;
	
	public TntEntity(@Nullable LivingEntity causingEntity) {
		super(EntityType.TNT);
		this.causingEntity = causingEntity;
		
		double angle = ThreadLocalRandom.current().nextDouble() * 6.2831854820251465;
		setVelocity(new Vec(-Math.sin(angle) * 0.02, 0.2f, -Math.cos(angle) * 0.02)
				.mul(ServerFlag.SERVER_TICKS_PER_SECOND));
		setFuse(80);
	}
	
	public int getFuse() {
		return ((PrimedTntMeta) getEntityMeta()).getFuseTime();
	}
	
	public void setFuse(int fuse) {
		((PrimedTntMeta) getEntityMeta()).setFuseTime(fuse);
	}
	
	@Override
	public void update(long time) {
		if (onGround) velocity = velocity.mul(0.7, -0.5, 0.7);
		int newFuse = getFuse() - 1;
		setFuse(newFuse);
		if (newFuse <= 0) {
			remove();
			if (instance.getExplosionSupplier() != null) instance.explode(
					(float) position.x(),
					(float) EntityUtils.getBodyY(this, 0.0625),
					(float) position.z(),
					4.0f,
					causingEntity == null ? null
							: NBT.Compound(NBT -> NBT.setString("causingEntity", causingEntity.getUuid().toString()))
			);
		}
	}
	
	@Override
	public double getEyeHeight() {
		return 0.15;
	}
}

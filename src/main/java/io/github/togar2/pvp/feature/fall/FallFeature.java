package io.github.togar2.pvp.feature.fall;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;

public interface FallFeature extends CombatFeature {
	FallFeature NO_OP = new FallFeature() {
		@Override
		public int getFallDamage(LivingEntity entity, double fallDistance) {
			return 0;
		}
		
		@Override
		public double getFallDistance(LivingEntity entity) {
			return 0;
		}
		
		@Override
		public void resetFallDistance(LivingEntity entity) {
		}
		
		@Override
		public Block getLastClimbedBlock(LivingEntity entity) {
			return Block.AIR;
		}
	};
	
	int getFallDamage(LivingEntity entity, double fallDistance);
	
	double getFallDistance(LivingEntity entity);
	
	void resetFallDistance(LivingEntity entity);
	
	Block getLastClimbedBlock(LivingEntity entity);
}

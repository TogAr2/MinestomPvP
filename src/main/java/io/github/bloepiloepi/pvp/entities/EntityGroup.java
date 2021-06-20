package io.github.bloepiloepi.pvp.entities;

import net.minestom.server.entity.LivingEntity;

public enum EntityGroup {
	DEFAULT,
	UNDEAD,
	ARTHROPOD,
	ILLAGER,
	AQUATIC;
	
	public static EntityGroup ofEntity(LivingEntity entity) {
		switch (entity.getEntityType()) {
			case BEE:
			case CAVE_SPIDER:
			case ENDERMITE:
			case SILVERFISH:
			case SPIDER:
				return EntityGroup.ARTHROPOD;
			case COD:
			case DOLPHIN:
			case ELDER_GUARDIAN:
			case GUARDIAN:
			case PUFFERFISH:
			case SALMON:
			case SQUID:
			case TROPICAL_FISH:
			case TURTLE:
				return EntityGroup.AQUATIC;
			case DROWNED:
			case HUSK:
			case PHANTOM:
			case SKELETON:
			case SKELETON_HORSE:
			case STRAY:
			case WITHER:
			case WITHER_SKELETON:
			case ZOGLIN:
			case ZOMBIE:
			case ZOMBIE_HORSE:
			case ZOMBIE_VILLAGER:
			case ZOMBIFIED_PIGLIN:
				return EntityGroup.UNDEAD;
			case EVOKER:
			case ILLUSIONER:
			case PILLAGER:
			case VINDICATOR:
				return EntityGroup.ILLAGER;
			default:
				return EntityGroup.DEFAULT;
		}
	}
	
	public boolean isUndead() {
		return this == UNDEAD;
	}
}

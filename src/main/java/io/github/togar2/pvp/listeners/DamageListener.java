package io.github.togar2.pvp.listeners;

import io.github.togar2.pvp.config.DamageConfig;
import io.github.togar2.pvp.config.PvPConfig;
import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.entity.EntityUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;

public class DamageListener {
	public static EventNode<EntityInstanceEvent> events(DamageConfig config) {
		EventNode<EntityInstanceEvent> node = EventNode.type("damage-events", PvPConfig.ENTITY_INSTANCE_FILTER);
		
		node.addListener(EntityDamageEvent.class, event -> config.getDamageHandler().handleEvent(event, config));
		
		if (config.isFallDamageEnabled()) {
			node.addListener(EntityTickEvent.class, event -> {
				if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
				if (livingEntity instanceof Player) return;
				Pos previousPosition = EntityUtils.getPreviousPosition(livingEntity);
				if (previousPosition == null) return;
				config.getFallDamageHandler().handleFallDamage(
						livingEntity, previousPosition,
						livingEntity.getPosition(), livingEntity.isOnGround()
				);
			});
			node.addListener(PlayerMoveEvent.class, event -> {
				Player player = event.getPlayer();
				config.getFallDamageHandler().handleFallDamage(
						player, player.getPosition(),
						event.getNewPosition(), event.isOnGround()
				);
			});
		}
		
		return node;
	}
	
	private static void displayAnimation(LivingEntity entity, DamageType type, DamageTypeInfo typeInfo,
	                                     boolean shield, DamageConfig config) {
		if (shield) {
			entity.triggerStatus((byte) 29);
			return;
		}
		if (!config.isDamageAnimation()) return;
		
		if (typeInfo.thorns()) {
			entity.triggerStatus((byte) 33);
			return;
		}
		
		byte status;
		if (type == DamageType.DROWN) {
			//Drown sound and animation
			status = 36;
		} else if (typeInfo.fire()) {
			//Burn sound and animation
			status = 37;
		} else if (type == DamageType.SWEET_BERRY_BUSH) {
			//Sweet berry bush sound and animation
			status = 44;
		} else if (type == DamageType.FREEZE) {
			//Freeze sound and animation
			status = 57;
		} else {
			//Damage sound and animation
			status = 2;
		}
		
		entity.triggerStatus(status);
	}
}

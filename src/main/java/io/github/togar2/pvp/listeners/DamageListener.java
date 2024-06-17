package io.github.togar2.pvp.listeners;

import io.github.togar2.pvp.config.DamageConfig;
import io.github.togar2.pvp.config.PvPConfig;
import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.entity.Tracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
		
		if (config.isDeathMessagesEnabled()) {
			node.addListener(PlayerDeathEvent.class, event -> {
				Component message = Tracker.combatManager.get(event.getPlayer().getUuid()).getDeathMessage();
				event.setChatMessage(message);
				event.setDeathText(message);
			});
		}
		
		return node;
	}
	
	private static final Component BAD_RESPAWN_POINT_MESSAGE = Component.text("[")
			.append(Component.translatable("death.attack.badRespawnPoint.link")
					.clickEvent(ClickEvent.openUrl("https://bugs.mojang.com/browse/MCPE-28723"))
					.hoverEvent(HoverEvent.showText(Component.text("MCPE-28723"))))
			.append(Component.text("]"));
	
	public static Component getAttackDeathMessage(@NotNull Player killed, @NotNull Damage damage) {
		if (damage.getType() == DamageType.BAD_RESPAWN_POINT) {
			return Component.translatable("death.attack.badRespawnPoint.message", killed.getName(), BAD_RESPAWN_POINT_MESSAGE);
		}
		
		String id = "death.attack." + MinecraftServer.getDamageTypeRegistry().get(damage.getType()).messageId();
		
		Entity source = damage.getSource();
		Entity attacker = damage.getAttacker();
		
		if (source != null) {
			Component ownerName = attacker == null ? EntityUtils.getName(source) : EntityUtils.getName(attacker);
			ItemStack weapon = source instanceof LivingEntity living ? living.getItemInMainHand() : ItemStack.AIR;
			if (!weapon.isAir() && weapon.get(ItemComponent.CUSTOM_NAME) != null) {
				return Component.translatable(id + ".item", EntityUtils.getName(killed), ownerName, weapon.get(ItemComponent.CUSTOM_NAME));
			} else {
				return Component.translatable(id, EntityUtils.getName(killed), ownerName);
			}
		} else {
			LivingEntity killer = getKillCredit(killed);
			if (killer == null) {
				return Component.translatable(id, EntityUtils.getName(killed));
			} else {
				return Component.translatable(id + ".player", EntityUtils.getName(killed),
						EntityUtils.getName(killer));
			}
		}
	}
	
	public static @Nullable LivingEntity getKillCredit(@NotNull Player killed) {
		LivingEntity killer = Tracker.combatManager.get(killed.getUuid()).getKiller();
		if (killer == null) {
			Integer lastDamagedById = killed.getTag(DamageHandler.LAST_DAMAGED_BY);
			if (lastDamagedById != null) {
				Entity entity = EntityUtils.findEntityById(lastDamagedById);
				if (entity instanceof LivingEntity living) killer = living;
			}
		}
		
		return killer;
	}
	
	private static void displayAnimation(LivingEntity entity, DynamicRegistry.Key<DamageType> type, DamageTypeInfo typeInfo,
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

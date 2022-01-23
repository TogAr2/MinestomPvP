package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.enums.ArmorMaterial;
import io.github.bloepiloepi.pvp.enums.Tool;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArmorToolListener {
	
	public static EventNode<EntityEvent> events(boolean legacy) {
		EventNode<EntityEvent> node = EventNode.type("armor-tool-events", EventFilter.ENTITY);
		
		node.addListener(EntityEquipEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
			
			if (event.getSlot().isArmor()) {
				changeArmorModifiers(livingEntity, event.getSlot(), event.getEquippedItem(), legacy);
			} else if (event.getSlot().isHand()) {
				changeHandModifiers(livingEntity, event.getSlot(), event.getEquippedItem(), legacy);
			}
		});
		
		node.addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class).handler(event ->
				changeHandModifiers(event.getPlayer(), EquipmentSlot.MAIN_HAND,
						event.getPlayer().getInventory().getItemStack(event.getSlot()), legacy))
				.build());
		
		return node;
	}
	
	private static void changeArmorModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack newItem, boolean legacy) {
		//Remove previous armor
		ItemStack previousStack = entity.getEquipment(slot);
		ArmorMaterial material = ArmorMaterial.fromMaterial(previousStack.getMaterial());
		removeAttributeModifiers(entity, ArmorMaterial.getAttributeIds(material, slot, previousStack));
		
		//Add new armor
		material = ArmorMaterial.fromMaterial(newItem.getMaterial());
		addAttributeModifiers(entity, ArmorMaterial.getAttributes(material, slot, newItem, legacy));
	}
	
	private static void changeHandModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack newItem, boolean legacy) {
		//Remove previous attribute modifiers
		ItemStack previousStack = entity.getEquipment(slot);
		Tool tool = Tool.fromMaterial(previousStack.getMaterial());
		removeAttributeModifiers(entity, Tool.getAttributeIds(tool, slot, previousStack));
		
		//Add new attribute modifiers
		tool = Tool.fromMaterial(newItem.getMaterial());
		addAttributeModifiers(entity, Tool.getAttributes(tool, slot, newItem, legacy));
	}
	
	private static void removeAttributeModifiers(LivingEntity entity, Map<Attribute, List<UUID>> modifiers) {
		for (Map.Entry<Attribute, List<UUID>> entry : modifiers.entrySet()) {
			AttributeInstance attribute = entity.getAttribute(entry.getKey());
			List<AttributeModifier> toRemove = new ArrayList<>();
			
			List<UUID> idsToRemove = entry.getValue();
			
			attribute.getModifiers().forEach((modifier) -> {
				if (idsToRemove.contains(modifier.getId())) {
					toRemove.add(modifier);
				}
			});
			
			toRemove.forEach(attribute::removeModifier);
		}
	}
	
	private static void addAttributeModifiers(LivingEntity entity, Map<Attribute, List<AttributeModifier>> modifiers) {
		for (Map.Entry<Attribute, List<AttributeModifier>> entry : modifiers.entrySet()) {
			AttributeInstance attribute = entity.getAttribute(entry.getKey());
			for (AttributeModifier modifier : entry.getValue()) {
				attribute.addModifier(modifier);
			}
		}
	}
}

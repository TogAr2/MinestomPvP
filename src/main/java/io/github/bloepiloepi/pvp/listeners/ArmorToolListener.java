package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.enums.ArmorMaterial;
import io.github.bloepiloepi.pvp.enums.Tool;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArmorToolListener {
	
	public static void register(EventNode<? super EntityEvent> eventNode) {
		EventNode<EntityEvent> node = EventNode.type("armor-tool-events", EventFilter.ENTITY);
		eventNode.addChild(node);
		
		node.addListener(EntityEquipEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			LivingEntity livingEntity = (LivingEntity) event.getEntity();
			
			if (event.getSlot().isArmor()) {
				changeArmorModifiers(livingEntity, event.getSlot(), event.getEquippedItem());
			} else if (event.getSlot().isHand()) {
				changeHandModifiers(livingEntity, event.getSlot(), event.getEquippedItem());
			}
		});
		
		node.addListener(PlayerChangeHeldSlotEvent.class, event ->
				changeHandModifiers(event.getPlayer(), EquipmentSlot.MAIN_HAND, event.getPlayer().getInventory().getItemStack(event.getSlot())));
	}
	
	private static void changeArmorModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack newItem) {
		//Remove previous armor
		ItemStack previousStack = entity.getEquipment(slot);
		ArmorMaterial material = ArmorMaterial.fromMaterial(previousStack.getMaterial());
		if (material != null) {
			removeAttributeModifiers(entity, material.getAttributes(slot, previousStack));
		}
		
		//Add new armor
		material = ArmorMaterial.fromMaterial(newItem.getMaterial());
		if (material != null) {
			addAttributeModifiers(entity, material.getAttributes(slot, newItem));
		}
	}
	
	private static void changeHandModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack newItem) {
		//Remove previous attribute modifiers
		ItemStack previousStack = entity.getEquipment(slot);
		Tool tool = Tool.fromMaterial(previousStack.getMaterial());
		if (tool != null) {
			removeAttributeModifiers(entity, tool.getAttributes(slot, previousStack));
		}
		
		//Add new attribute modifiers
		tool = Tool.fromMaterial(newItem.getMaterial());
		if (tool != null) {
			addAttributeModifiers(entity, tool.getAttributes(slot, newItem));
		}
	}
	
	private static void removeAttributeModifiers(LivingEntity entity, Map<Attribute, AttributeModifier> modifiers) {
		boolean sprint = entity.isSprinting();
		
		for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entrySet()) {
			AttributeInstance attribute = entity.getAttribute(entry.getKey());
			
			List<AttributeModifier> toRemove = new ArrayList<>();
			attribute.getModifiers().forEach((modifier) -> {
				if (modifier.getId().equals(entry.getValue().getId())) {
					toRemove.add(modifier);
				}
			});
			
			toRemove.forEach(attribute::removeModifier);
		}
		
		entity.setSprinting(sprint);
	}
	
	private static void addAttributeModifiers(LivingEntity entity, Map<Attribute, AttributeModifier> modifiers) {
		boolean sprint = entity.isSprinting();
		
		for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entrySet()) {
			AttributeInstance attribute = entity.getAttribute(entry.getKey());
			attribute.addModifier(entry.getValue());
		}
		
		entity.setSprinting(sprint); //TODO ??????????
	}
}

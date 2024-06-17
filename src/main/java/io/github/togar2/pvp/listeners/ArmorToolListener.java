package io.github.togar2.pvp.listeners;

import io.github.togar2.pvp.config.ArmorToolConfig;
import io.github.togar2.pvp.config.PvPConfig;
import io.github.togar2.pvp.enums.ArmorMaterial;
import io.github.togar2.pvp.enums.Tool;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArmorToolListener {
	
	public static EventNode<EntityInstanceEvent> events(ArmorToolConfig config) {
		EventNode<EntityInstanceEvent> node = EventNode.type("armor-tool-events", PvPConfig.ENTITY_INSTANCE_FILTER);
		
		if (config.isArmorModifiersEnabled()) node.addListener(EntityEquipEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
			
			if (event.getSlot().isArmor()) {
				changeArmorModifiers(livingEntity, event.getSlot(), event.getEquippedItem(), config.isLegacy());
			} else if (event.getSlot().isHand()) {
				changeHandModifiers(livingEntity, event.getSlot(), event.getEquippedItem(), config.isLegacy());
			}
		});
		
		if (config.isToolModifiersEnabled()) node.addListener(PlayerChangeHeldSlotEvent.class, event ->
				changeHandModifiers(event.getPlayer(), EquipmentSlot.MAIN_HAND,
						event.getPlayer().getInventory().getItemStack(event.getSlot()), config.isLegacy()));
		
		return node;
	}
	
	private static void changeArmorModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack newItem, boolean legacy) {
		//Remove previous armor
		ItemStack previousStack = entity.getEquipment(slot);
		ArmorMaterial material = ArmorMaterial.fromMaterial(previousStack.material());
		removeAttributeModifiers(entity, ArmorMaterial.getAttributeIds(material, slot, previousStack));
		
		//Add new armor
		material = ArmorMaterial.fromMaterial(newItem.material());
		addAttributeModifiers(entity, ArmorMaterial.getAttributes(material, slot, newItem, legacy));
	}
	
	private static void changeHandModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack newItem, boolean legacy) {
		//Remove previous attribute modifiers
		ItemStack previousStack = entity.getEquipment(slot);
		Tool tool = Tool.fromMaterial(previousStack.material());
		removeAttributeModifiers(entity, Tool.getAttributeIds(tool, slot, previousStack));
		
		//Add new attribute modifiers
		tool = Tool.fromMaterial(newItem.material());
		addAttributeModifiers(entity, Tool.getAttributes(tool, slot, newItem, legacy));
	}
	
	private static void removeAttributeModifiers(LivingEntity entity, Map<Attribute, List<NamespaceID>> modifiers) {
		for (Map.Entry<Attribute, List<NamespaceID>> entry : modifiers.entrySet()) {
			AttributeInstance attribute = entity.getAttribute(entry.getKey());
			List<AttributeModifier> toRemove = new ArrayList<>();
			
			List<NamespaceID> idsToRemove = entry.getValue();
			
			attribute.modifiers().forEach((modifier) -> {
				if (idsToRemove.contains(modifier.id())) {
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

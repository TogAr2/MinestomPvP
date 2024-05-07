package io.github.togar2.pvp.feature.attributes;

import io.github.togar2.pvp.enums.ArmorMaterial;
import io.github.togar2.pvp.enums.Tool;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataFeatureImpl implements DataFeature<Attribute> {
	//TODO this probably shouldn't work this way
	// We probably want to store all the tools & armor separately per DataFeature
	private final boolean legacy;
	
	public DataFeatureImpl(boolean legacy) {
		this.legacy = legacy;
	}
	
	@Override
	public void init(EventNode<Event> node) {
		node.addListener(EntityEquipEvent.class, this::onEquip);
		node.addListener(PlayerChangeHeldSlotEvent.class, event -> changeHandModifiers(
				event.getPlayer(), EquipmentSlot.MAIN_HAND,
				event.getPlayer().getInventory().getItemStack(event.getSlot())
		));
	}
	
	public void onEquip(EntityEquipEvent event) {
		if (!(event.getEntity() instanceof LivingEntity entity)) return;
		
		//TODO all things related to tools and attributes need an overhaul. This is temporary
		if (event.getSlot().isArmor()) {
			changeArmorModifiers(entity, event.getSlot(), event.getEquippedItem());
		} else if (event.getSlot().isHand()) {
			changeHandModifiers(entity, event.getSlot(), event.getEquippedItem());
		}
	}
	
	@Override
	public double getValue(LivingEntity entity, Attribute attribute) {
		return entity.getAttribute(attribute).getValue();
	}
	
	private void changeArmorModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack newItem) {
		//Remove previous armor
		ItemStack previousStack = entity.getEquipment(slot);
		ArmorMaterial material = ArmorMaterial.fromMaterial(previousStack.material());
		removeAttributeModifiers(entity, ArmorMaterial.getAttributeIds(material, slot, previousStack));
		
		//Add new armor
		material = ArmorMaterial.fromMaterial(newItem.material());
		addAttributeModifiers(entity, ArmorMaterial.getAttributes(material, slot, newItem, legacy));
	}
	
	private void changeHandModifiers(LivingEntity entity, EquipmentSlot slot, ItemStack newItem) {
		//Remove previous attribute modifiers
		ItemStack previousStack = entity.getEquipment(slot);
		Tool tool = Tool.fromMaterial(previousStack.material());
		removeAttributeModifiers(entity, Tool.getAttributeIds(tool, slot, previousStack));
		
		//Add new attribute modifiers
		tool = Tool.fromMaterial(newItem.material());
		addAttributeModifiers(entity, Tool.getAttributes(tool, slot, newItem, legacy));
	}
	
	private void removeAttributeModifiers(LivingEntity entity, Map<Attribute, List<UUID>> modifiers) {
		for (Map.Entry<Attribute, List<UUID>> entry : modifiers.entrySet()) {
			AttributeInstance attribute = entity.getAttribute(entry.getKey());
			for (UUID uuid : entry.getValue()) {
				attribute.removeModifier(uuid);
			}
		}
	}
	
	private void addAttributeModifiers(LivingEntity entity, Map<Attribute, List<AttributeModifier>> modifiers) {
		for (Map.Entry<Attribute, List<AttributeModifier>> entry : modifiers.entrySet()) {
			AttributeInstance attribute = entity.getAttribute(entry.getKey());
			for (AttributeModifier modifier : entry.getValue()) {
				attribute.addModifier(modifier);
			}
		}
	}
}

package io.github.togar2.pvp.enums;

import io.github.togar2.pvp.utils.ModifierId;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum ArmorMaterial {
	LEATHER(new int[]{1, 2, 3, 1}, new int[]{1, 3, 2, 1}, SoundEvent.ITEM_ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET),
	CHAIN(new int[]{1, 4, 5, 2}, new int[]{2, 5, 4, 1}, SoundEvent.ITEM_ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET),
	IRON(new int[]{2, 5, 6, 2}, new int[]{2, 6, 5, 2}, SoundEvent.ITEM_ARMOR_EQUIP_IRON, 0.0F, 0.0F, Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET),
	GOLD(new int[]{1, 3, 5, 2}, new int[]{2, 5, 3, 1}, SoundEvent.ITEM_ARMOR_EQUIP_GOLD, 0.0F, 0.0F, Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET),
	DIAMOND(new int[]{3, 6, 8, 3}, new int[]{3, 8, 6, 3}, SoundEvent.ITEM_ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET),
	TURTLE(new int[]{2, 5, 6, 2}, new int[]{2, 6, 5, 2}, SoundEvent.ITEM_ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, Material.TURTLE_HELMET),
	NETHERITE(new int[]{3, 6, 8, 3}, new int[]{3, 8, 6, 3}, SoundEvent.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, Material.NETHERITE_BOOTS, Material.NETHERITE_LEGGINGS, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_HELMET);
	
	private final int[] protectionAmounts;
	private final int[] legacyProtectionAmounts;
	private final SoundEvent equipSound;
	private final float toughness;
	private final float knockbackResistance;
	private final Material[] items;
	
	ArmorMaterial(int[] protectionAmounts, int[] legacyProtectionAmounts, SoundEvent equipSound, float toughness, float knockbackResistance, Material... items) {
		this.protectionAmounts = protectionAmounts;
		this.legacyProtectionAmounts = legacyProtectionAmounts;
		this.equipSound = equipSound;
		this.toughness = toughness;
		this.knockbackResistance = knockbackResistance;
		this.items = items;
	}
	
	public int getProtectionAmount(EquipmentSlot slot, boolean legacy) {
		int id;
		switch (slot) {
			case HELMET: id = 3; break;
			case CHESTPLATE: id = 2; break;
			case LEGGINGS: id = 1; break;
			case BOOTS: id = 0; break;
			default: return 0;
		}
		
		return legacy ? this.legacyProtectionAmounts[id] : this.protectionAmounts[id];
	}
	
	public SoundEvent getEquipSound() {
		return this.equipSound;
	}
	
	public float getToughness() {
		return this.toughness;
	}
	
	public float getKnockbackResistance() {
		return this.knockbackResistance;
	}
	
	public static Map<Attribute, List<AttributeModifier>> getAttributes(@Nullable ArmorMaterial material, EquipmentSlot slot, ItemStack item, boolean legacy) {
		Map<Attribute, List<AttributeModifier>> modifiers = new HashMap<>();
		for (AttributeList.Modifier itemModifier : item.get(ItemComponent.ATTRIBUTE_MODIFIERS).modifiers()) {
			if (itemModifier.slot().contains(slot)) {
				modifiers.computeIfAbsent(itemModifier.attribute(), k -> new ArrayList<>())
						.add(itemModifier.modifier());
			}
		}
		
		// Only add armor attributes if the material is armor
		if (material != null) {
			if (slot == getRequiredSlot(item.material())) {
				NamespaceID modifierId = getModifierId(slot);
				modifiers.computeIfAbsent(Attribute.GENERIC_ARMOR, k -> new ArrayList<>()).add(new AttributeModifier(modifierId, material.getProtectionAmount(slot, legacy), AttributeOperation.ADD_VALUE));
				modifiers.computeIfAbsent(Attribute.GENERIC_ARMOR_TOUGHNESS, k -> new ArrayList<>()).add(new AttributeModifier(modifierId, material.toughness, AttributeOperation.ADD_VALUE));
				if (material.knockbackResistance > 0) {
					modifiers.computeIfAbsent(Attribute.GENERIC_KNOCKBACK_RESISTANCE, k -> new ArrayList<>()).add(new AttributeModifier(modifierId, material.knockbackResistance, AttributeOperation.ADD_VALUE));
				}
			}
		}
		
		return modifiers;
	}
	
	public static Map<Attribute, List<NamespaceID>> getAttributeIds(@Nullable ArmorMaterial material, EquipmentSlot slot, ItemStack item) {
		Map<Attribute, List<NamespaceID>> modifiers = new HashMap<>();
		for (AttributeList.Modifier itemModifier : item.get(ItemComponent.ATTRIBUTE_MODIFIERS).modifiers()) {
			if (itemModifier.slot().contains(slot)) {
				modifiers.computeIfAbsent(itemModifier.attribute(), k -> new ArrayList<>()).add(itemModifier.modifier().id());
			}
		}
		
		if (material != null) {
			if (slot == getRequiredSlot(item.material())) {
				NamespaceID modifierId = getModifierId(slot);
				modifiers.computeIfAbsent(Attribute.GENERIC_ARMOR, k -> new ArrayList<>()).add(modifierId);
				modifiers.computeIfAbsent(Attribute.GENERIC_ARMOR_TOUGHNESS, k -> new ArrayList<>()).add(modifierId);
				modifiers.computeIfAbsent(Attribute.GENERIC_KNOCKBACK_RESISTANCE, k -> new ArrayList<>()).add(modifierId);
			}
		}
		
		return modifiers;
	}
	
	public static EquipmentSlot getRequiredSlot(Material material) {
		EquipmentSlot slot = material.registry().equipmentSlot();
		return slot == null ? EquipmentSlot.HELMET : slot;
	}
	
	private static final Map<Material, ArmorMaterial> MATERIAL_TO_ARMOR_MATERIAL = new HashMap<>();
	
	public static ArmorMaterial fromMaterial(Material material) {
		return MATERIAL_TO_ARMOR_MATERIAL.get(material);
	}
	
	public static NamespaceID getModifierId(EquipmentSlot slot) {
		return ModifierId.ARMOR_MODIFIERS[slot.ordinal() - 2];
	}
	
	static {
		for (ArmorMaterial armorMaterial : values()) {
			for (Material material : armorMaterial.items) {
				MATERIAL_TO_ARMOR_MATERIAL.put(material, armorMaterial);
			}
		}
	}
}

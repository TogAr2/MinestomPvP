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
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum Tool {
	WOODEN_SWORD(ToolMaterial.WOOD, 3, 4.0F, -2.4F, false, true),
	STONE_SWORD(ToolMaterial.STONE, 3, 4.0F, -2.4F, false, true),
	IRON_SWORD(ToolMaterial.IRON, 3, 4.0F, -2.4F, false, true),
	DIAMOND_SWORD(ToolMaterial.DIAMOND, 3, 4.0F, -2.4F, false, true),
	GOLDEN_SWORD(ToolMaterial.GOLD, 3, 4.0F, -2.4F, false, true),
	NETHERITE_SWORD(ToolMaterial.NETHERITE, 3, 4.0F, -2.4F, false, true),
	
	WOODEN_SHOVEL(ToolMaterial.WOOD, 1.5F, 1.0F, -3.0F),
	STONE_SHOVEL(ToolMaterial.STONE, 1.5F, 1.0F, -3.0F),
	IRON_SHOVEL(ToolMaterial.IRON, 1.5F, 1.0F, -3.0F),
	DIAMOND_SHOVEL(ToolMaterial.DIAMOND, 1.5F, 1.0F, -3.0F),
	GOLDEN_SHOVEL(ToolMaterial.GOLD, 1.5F, 1.0F, -3.0F),
	NETHERITE_SHOVEL(ToolMaterial.NETHERITE, 1.5F, 1.0F, -3.0F),
	
	WOODEN_PICKAXE(ToolMaterial.WOOD, 1, 2.0F, -2.8F),
	STONE_PICKAXE(ToolMaterial.STONE, 1, 2.0F, -2.8F),
	IRON_PICKAXE(ToolMaterial.IRON, 1, 2.0F, -2.8F),
	DIAMOND_PICKAXE(ToolMaterial.DIAMOND, 1, 2.0F, -2.8F),
	GOLDEN_PICKAXE(ToolMaterial.GOLD, 1, 2.0F, -2.8F),
	NETHERITE_PICKAXE(ToolMaterial.NETHERITE, 1, 2.0F, -2.8F),
	
	WOODEN_AXE(ToolMaterial.WOOD, 6.0F, 3.0F, -3.2F, true, false),
	STONE_AXE(ToolMaterial.STONE, 7.0F, 3.0F, -3.2F, true, false),
	IRON_AXE(ToolMaterial.IRON, 6.0F, 3.0F, -3.1F, true, false),
	DIAMOND_AXE(ToolMaterial.DIAMOND, 5.0F, 3.0F, -3.0F, true, false),
	GOLDEN_AXE(ToolMaterial.GOLD, 6.0F, 3.0F, -3.0F, true, false),
	NETHERITE_AXE(ToolMaterial.NETHERITE, 5.0F, 3.0F, -3.0F, true, false),
	
	// Attack damage for hoes is negative to disable the ToolMaterial attack damage
	WOODEN_HOE(ToolMaterial.WOOD, 0, 0, -3.0F),
	STONE_HOE(ToolMaterial.STONE, -1, -1, -2.0F),
	IRON_HOE(ToolMaterial.IRON, -2, -2, -1.0F),
	DIAMOND_HOE(ToolMaterial.DIAMOND, -3, -3, 0.0F),
	GOLDEN_HOE(ToolMaterial.GOLD, 0, 0, -3.0F),
	NETHERITE_HOE(ToolMaterial.NETHERITE, -4, -4, 0.0F),
	
	// We don't know the legacy attack damage for tridents, since they didn't exist
	// 5.0 seems to be balanced
	TRIDENT(null, 8.0F, 5.0F, -2.9000000953674316F);
	
	private final Material material;
	private boolean isAxe = false;
	private boolean isSword = false;
	
	private final Map<Attribute, AttributeModifier> attributeModifiers = new HashMap<>();
	private final Map<Attribute, AttributeModifier> legacyAttributeModifiers = new HashMap<>();
	
	Tool(@Nullable ToolMaterial toolMaterial, float attackDamage, float legacyAttackDamage, float attackSpeed) {
		float finalAttackDamage = attackDamage + (toolMaterial == null ? 0 : toolMaterial.getAttackDamage());
		float finalLegacyAttackDamage = legacyAttackDamage + (toolMaterial == null ? 0 : toolMaterial.getAttackDamage());
		this.material = Material.fromNamespaceId(this.name().toLowerCase());
		
		this.attributeModifiers.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(ModifierId.ATTACK_DAMAGE_MODIFIER_ID, finalAttackDamage, AttributeOperation.ADD_VALUE));
		this.attributeModifiers.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(ModifierId.ATTACK_SPEED_MODIFIER_ID, attackSpeed, AttributeOperation.ADD_VALUE));
		
		this.legacyAttributeModifiers.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(ModifierId.ATTACK_DAMAGE_MODIFIER_ID, finalLegacyAttackDamage, AttributeOperation.ADD_VALUE));
	}
	
	Tool(@Nullable ToolMaterial toolMaterial, float attackDamage, float legacyAttackDamage, float attackSpeed, boolean isAxe, boolean isSword) {
		this(toolMaterial, attackDamage, legacyAttackDamage, attackSpeed);
		this.isAxe = isAxe;
		this.isSword = isSword;
	}
	
	public static Map<Attribute, List<AttributeModifier>> getAttributes(@Nullable Tool tool, EquipmentSlot slot, ItemStack item, boolean legacy) {
		Map<Attribute, List<AttributeModifier>> modifiers = new HashMap<>();
		for (AttributeList.Modifier itemModifier : item.get(ItemComponent.ATTRIBUTE_MODIFIERS).modifiers()) {
			if (itemModifier.slot().contains(slot)) {
				modifiers.computeIfAbsent(itemModifier.attribute(), k -> new ArrayList<>())
						.add(itemModifier.modifier());
			}
		}
		
		// Only add tool attributes if the material is a tool
		if (tool != null) {
			//Weapon attributes (attack damage, etc) do not apply in offhand
			if (slot == EquipmentSlot.MAIN_HAND) {
				(legacy ? tool.legacyAttributeModifiers : tool.attributeModifiers).forEach((attribute, modifier) -> {
					modifiers.computeIfAbsent(attribute, k -> new ArrayList<>()).add(modifier);
				});
			}
		}
		
		return modifiers;
	}
	
	public static Map<Attribute, List<NamespaceID>> getAttributeIds(@Nullable Tool tool, EquipmentSlot slot, ItemStack item) {
		Map<Attribute, List<NamespaceID>> modifiers = new HashMap<>();
		for (AttributeList.Modifier itemModifier : item.get(ItemComponent.ATTRIBUTE_MODIFIERS).modifiers()) {
			if (itemModifier.slot().contains(slot)) {
				modifiers.computeIfAbsent(itemModifier.attribute(), k -> new ArrayList<>()).add(itemModifier.modifier().id());
			}
		}
		
		if (tool != null) {
			if (slot == EquipmentSlot.MAIN_HAND) {
				modifiers.computeIfAbsent(Attribute.GENERIC_ATTACK_DAMAGE, k -> new ArrayList<>()).add(ModifierId.ATTACK_DAMAGE_MODIFIER_ID);
				modifiers.computeIfAbsent(Attribute.GENERIC_ATTACK_SPEED, k -> new ArrayList<>()).add(ModifierId.ATTACK_SPEED_MODIFIER_ID);
			}
		}
		
		return modifiers;
	}
	
	public boolean isAxe() {
		return isAxe;
	}
	
	public boolean isSword() {
		return isSword;
	}
	
	public static Tool fromMaterial(Material material) {
		for (Tool tool : values()) {
			if (tool.material == material) {
				return tool;
			}
		}
		
		return null;
	}
}

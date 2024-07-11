package io.github.togar2.pvp.feature.attributes;

import io.github.togar2.pvp.enums.ArmorMaterial;
import io.github.togar2.pvp.enums.Tool;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemStack;

public class VanillaEquipmentFeature implements DataFeature<Attribute>, RegistrableFeature {
	public static final DefinedFeature<VanillaEquipmentFeature> DEFINED = new DefinedFeature<>(
			FeatureType.EQUIPMENT_DATA, VanillaEquipmentFeature::new,
			FeatureType.VERSION
	);
	
	private final FeatureConfiguration configuration;
	
	//TODO this probably shouldn't work this way
	// We probably want to store all the tools & armor separately per DataFeature
	private CombatVersion version;
	
	public VanillaEquipmentFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void initDependencies() {
		this.version = configuration.get(FeatureType.VERSION);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(EntityEquipEvent.class, this::onEquip);
		node.addListener(PlayerChangeHeldSlotEvent.class, event -> {
			LivingEntity entity = event.getPlayer();
			ItemStack newItem = event.getPlayer().getInventory().getItemStack(event.getSlot());
			Tool.updateEquipmentAttributes(entity, entity.getEquipment(EquipmentSlot.MAIN_HAND), newItem, EquipmentSlot.MAIN_HAND, version);
		});
	}
	
	protected void onEquip(EntityEquipEvent event) {
		if (!(event.getEntity() instanceof LivingEntity entity)) return;
		
		if (event.getSlot().isArmor()) {
			EquipmentSlot slot = event.getSlot();
			ArmorMaterial.updateEquipmentAttributes(entity, entity.getEquipment(slot), event.getEquippedItem(), slot, version);
		} else if (event.getSlot().isHand()) {
			EquipmentSlot slot = event.getSlot();
			Tool.updateEquipmentAttributes(entity, entity.getEquipment(slot), event.getEquippedItem(), slot, version);
		}
	}
	
	@Override
	public double getValue(LivingEntity entity, Attribute attribute) {
		return entity.getAttributeValue(attribute);
	}
}

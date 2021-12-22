package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.network.packet.server.play.EntityPropertiesPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.List;

@Mixin(EntityPropertiesPacket.class)
public class EntityPropertiesPacketMixin {
	
	@Shadow @Final private int entityId;
	
	@Shadow @Final private List<AttributeInstance> properties;
	
	/**
	 * @author me
	 */
	@Overwrite
	public void write(@NotNull BinaryWriter writer) {
		writer.writeVarInt(entityId);
		writer.writeVarInt(properties.size());
		for (AttributeInstance instance : properties) {
			final Attribute attribute = instance.getAttribute();
			
			writer.writeSizedString(attribute.getKey());
			writer.writeDouble(instance.getBaseValue()); // <--
			
			{
				Collection<AttributeModifier> modifiers = instance.getModifiers();
				writer.writeVarInt(modifiers.size());
				
				for (var modifier : modifiers) {
					writer.writeUuid(modifier.getId());
					writer.writeDouble(modifier.getAmount());
					writer.writeByte((byte) modifier.getOperation().getId());
				}
			}
		}
	}
}

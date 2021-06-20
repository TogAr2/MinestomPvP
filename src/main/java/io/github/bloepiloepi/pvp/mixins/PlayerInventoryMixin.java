package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
	
	@Shadow @Final protected Player player;
	
	@Inject(method = "clear", at = @At("HEAD"))
	private void onClear(CallbackInfo ci) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (!player.getEquipment(slot).isAir()) {
				EntityEquipEvent event = new EntityEquipEvent(player, ItemStack.AIR, slot);
				EventDispatcher.call(event);
			}
		}
	}
}

package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.item.Material;
import net.minestom.server.listener.UseItemListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(UseItemListener.class)
public class UseItemListenerMixin {
	
	//Also allow potions to be "eaten"
	@Redirect(method = "useItemListener", at = @At(value = "INVOKE", target = "Lnet/minestom/server/item/Material;isFood()Z"))
	private static boolean onIsFood(Material material) {
		return material.isFood() || material == Material.POTION;
	}
}

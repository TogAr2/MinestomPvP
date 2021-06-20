package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerMixin {
	
	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minestom/server/item/Material;isFood()Z"))
	private boolean onIsFood(Material material) {
		return material.isFood() || material == Material.POTION;
	}
}

package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.metadata.CrossbowMeta;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowMeta.Builder.class)
public class CrossbowMetaMixin {
	
	@Shadow private ItemStack projectile1;
	@Shadow private ItemStack projectile2;
	@Shadow private ItemStack projectile3;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		this.projectile1 = ItemStack.AIR;
		this.projectile2 = ItemStack.AIR;
		this.projectile3 = ItemStack.AIR;
	}
}

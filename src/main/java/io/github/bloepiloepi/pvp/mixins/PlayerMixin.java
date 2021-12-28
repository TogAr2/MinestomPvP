//package io.github.bloepiloepi.pvp.mixins;
//
//import io.github.bloepiloepi.pvp.potion.PotionListener;
//import net.minestom.server.entity.*;
//import org.jetbrains.annotations.NotNull;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(Player.class)
//public abstract class PlayerMixin {
//
//	@Inject(method = "setGameMode", at = @At(value = "TAIL"))
//	private void onSetGameMode(@NotNull GameMode gameMode, CallbackInfo ci) {
//		PotionListener.updatePotionVisibility((Player) (Object) this);
//	}
//}

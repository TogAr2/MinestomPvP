package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.item.Enchantment;
import net.minestom.server.utils.NBTUtils;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(NBTUtils.class)
public class NBTUtilsMixin {
	
	@Inject(method = "writeEnchant", at = @At(value = "HEAD"), cancellable = true)
	private static void writeEnchant(NBTCompound nbt, String listName, Map<Enchantment, Short> enchantmentMap, CallbackInfo ci) {
		ci.cancel();
		
		NBTList<NBTCompound> enchantList = new NBTList<>(NBTTypes.TAG_Compound);
		for (Map.Entry<Enchantment, Short> entry : enchantmentMap.entrySet()) {
			final Enchantment enchantment = entry.getKey();
			final short level = entry.getValue();
			
			enchantList.add(new NBTCompound()
					.setShort("lvl", level)
					.setString("id", enchantment.name())
			);
		}
		nbt.set(listName, enchantList);
	}
}

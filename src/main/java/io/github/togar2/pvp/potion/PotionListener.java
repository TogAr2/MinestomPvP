package io.github.togar2.pvp.potion;

import io.github.togar2.pvp.potion.item.CustomPotionType;
import io.github.togar2.pvp.potion.item.CustomPotionTypes;
import io.github.togar2.pvp.utils.PotionUtils;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PotionListener {
	//todo these methods should be a feature
	public static int getColor(ItemStack stack, boolean legacy) {
		PotionMeta meta = stack.meta(PotionMeta.class);
		if (meta.getColor() != null) {
			return meta.getColor().asRGB();
		} else {
			return meta.getPotionType() == PotionType.EMPTY ? 16253176 : PotionUtils.getPotionColor(getAllPotions(meta, legacy));
		}
	}
	
	public static List<Potion> getAllPotions(PotionMeta meta, boolean legacy) {
		return getAllPotions(meta.getPotionType(), meta.getCustomPotionEffects(), legacy);
	}
	
	public static List<Potion> getAllPotions(PotionType potionType,
	                                         Collection<net.minestom.server.potion.CustomPotionEffect> customEffects,
	                                         boolean legacy) {
		//PotionType effects plus custom effects
		List<Potion> potions = new ArrayList<>();
		
		CustomPotionType customPotionType = CustomPotionTypes.get(potionType);
		if (customPotionType != null) {
			potions.addAll(legacy ? customPotionType.getLegacyEffects() : customPotionType.getEffects());
		}
		
		potions.addAll(customEffects.stream().map((customPotion) ->
				new Potion(Objects.requireNonNull(PotionEffect.fromId(customPotion.id())),
						customPotion.amplifier(), customPotion.duration(),
						PotionUtils.createFlags(
								customPotion.isAmbient(),
								customPotion.showParticles(),
								customPotion.showIcon()
						))).toList());
		
		return potions;
	}
	
	private static final byte DEFAULT_FLAGS = PotionUtils.createFlags(false, true, true);
	public static byte defaultFlags() {
		return DEFAULT_FLAGS;
	}
}

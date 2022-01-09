package io.github.bloepiloepi.pvp.damage.combat;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EndCombatEventPacket;
import net.minestom.server.network.packet.server.play.EnterCombatEventPacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CombatManager {
	private final List<CombatEntry> entries = new ArrayList<>();
	private final Player player;
	private long lastDamageTime;
	private long combatStartTime;
	private long combatEndTime;
	private boolean inCombat;
	private boolean takingDamage;
	private String nextFallLocation;
	
	public CombatManager(Player player) {
		this.player = player;
	}
	
	public void prepareForDamage() {
		nextFallLocation = null;
		Block lastClimbedBlock = Tracker.lastClimbedBlock.get(player.getUuid());
		if (lastClimbedBlock == null) {
			//TODO check for water at feet
			return;
		}
		
		if (lastClimbedBlock == Block.LADDER || lastClimbedBlock == Block.ACACIA_TRAPDOOR
				|| lastClimbedBlock == Block.BIRCH_TRAPDOOR || lastClimbedBlock == Block.CRIMSON_TRAPDOOR
				|| lastClimbedBlock == Block.IRON_TRAPDOOR || lastClimbedBlock == Block.DARK_OAK_TRAPDOOR
				|| lastClimbedBlock == Block.JUNGLE_TRAPDOOR || lastClimbedBlock == Block.OAK_TRAPDOOR
				|| lastClimbedBlock == Block.SPRUCE_TRAPDOOR || lastClimbedBlock == Block.WARPED_TRAPDOOR) {
			nextFallLocation = "ladder";
			return;
		}
		
		if (lastClimbedBlock == Block.VINE) {
			nextFallLocation = "vines";
			return;
		}
		
		if (lastClimbedBlock == Block.WEEPING_VINES || lastClimbedBlock == Block.WEEPING_VINES_PLANT) {
			nextFallLocation = "weeping_vines";
			return;
		}
		
		if (lastClimbedBlock == Block.TWISTING_VINES || lastClimbedBlock == Block.TWISTING_VINES_PLANT) {
			nextFallLocation = "twisting_vines";
			return;
		}
		
		if (lastClimbedBlock == Block.SCAFFOLDING) {
			nextFallLocation = "scaffolding";
			return;
		}
		
		nextFallLocation = "other_climbable";
	}
	
	public void recordDamage(CustomDamageType damageType, float damage) {
		recheckStatus();
		prepareForDamage();
		
		//TODO falldistance
		CombatEntry entry = new CombatEntry(damageType, damage, nextFallLocation, 0);
		entries.add(entry);
		
		lastDamageTime = System.currentTimeMillis();
		takingDamage = true;
		
		if (entry.isCombat() && !inCombat && !player.isDead()) {
			inCombat = true;
			combatStartTime = System.currentTimeMillis();
			combatEndTime = combatStartTime;
			
			onEnterCombat();
		}
	}
	
	public Component getDeathMessage() {
		if (entries.isEmpty()) {
			return Component.translatable("death.attack.generic", getEntityName());
		}
		
		CombatEntry heaviestFall = null;
		CombatEntry lastEntry = entries.get(entries.size() - 1);
		
		boolean fall = false;
		if (lastEntry.getDamageType() == CustomDamageType.FALL) {
			heaviestFall = getHeaviestFall();
			fall = heaviestFall != null;
		}
		
		if (!fall) {
			return lastEntry.getDamageType().getDeathMessage(player);
		}
		
		if (heaviestFall.getDamageType() == CustomDamageType.FALL || heaviestFall.getDamageType() == CustomDamageType.OUT_OF_WORLD) {
			return Component.translatable("death.fell.accident." + heaviestFall.getMessageFallLocation(), getEntityName());
		}
		
		Entity firstAttacker = heaviestFall.getAttacker();
		Entity lastAttacker = lastEntry.getAttacker();
		
		if (firstAttacker != null && firstAttacker != lastAttacker) {
			ItemStack weapon = firstAttacker instanceof LivingEntity ? ((LivingEntity) firstAttacker).getItemInMainHand() : ItemStack.AIR;
			if (!weapon.isAir() && weapon.getDisplayName() != null) {
				return Component.translatable("death.fell.assist.item", getEntityName(), EntityUtils.getName(firstAttacker), weapon.getDisplayName());
			} else {
				return Component.translatable("death.fell.assist", getEntityName(), EntityUtils.getName(firstAttacker));
			}
		} else if (lastAttacker != null) {
			ItemStack weapon = lastAttacker instanceof LivingEntity ? ((LivingEntity) lastAttacker).getItemInMainHand() : ItemStack.AIR;
			if (!weapon.isAir() && weapon.getDisplayName() != null) {
				return Component.translatable("death.fell.finish.item", getEntityName(), EntityUtils.getName(lastAttacker), weapon.getDisplayName());
			} else {
				return Component.translatable("death.fell.finish", getEntityName(), EntityUtils.getName(lastAttacker));
			}
		} else {
			return Component.translatable("death.fell.killer", getEntityName());
		}
	}
	
	public @Nullable LivingEntity getKiller() {
		LivingEntity entity = null;
		Player player = null;
		float livingDamage = 0.0F;
		float playerDamage = 0.0F;
		
		for (CombatEntry entry : entries) {
			Entity attacker = entry.getAttacker();
			if (attacker instanceof Player && (player == null || entry.getDamage() > playerDamage)) {
				player = (Player) attacker;
				playerDamage = entry.getDamage();
			} else if (attacker instanceof LivingEntity && (entity == null || entry.getDamage() <= livingDamage)) {
				entity = (LivingEntity) attacker;
				livingDamage = entry.getDamage();
			}
		}
		
		if (player != null && playerDamage >= livingDamage / 3.0F) {
			return player;
		}
		
		return entity;
	}
	
	private @Nullable CombatEntry getHeaviestFall() {
		CombatEntry mostDamageEntry = null;
		CombatEntry highestFallEntry = null;
		float mostDamage = 0.0F;
		float highestFall = 0.0F;
		
		for (int i = 0; i < entries.size(); i++) {
			CombatEntry entry = entries.get(i);
			
			if ((entry.getDamageType() == CustomDamageType.FALL || entry.getDamageType().isOutOfWorld())
					&& entry.getFallDistance() > 0.0F && (mostDamageEntry == null || entry.getFallDistance() > highestFall)) {
				if (i > 0) {
					mostDamageEntry = entries.get(i - 1);
				} else {
					mostDamageEntry = entry;
				}
				
				highestFall = entry.getFallDistance();
			}
			
			if (entry.getFallLocation() != null && (highestFallEntry == null || entry.getDamage() > mostDamage)) {
				highestFallEntry = entry;
				mostDamage = entry.getDamage();
			}
		}
		
		if (highestFall > 5.0F && mostDamageEntry != null) {
			return mostDamageEntry;
		} else if (mostDamage > 5.0F && highestFallEntry != null) {
			return highestFallEntry;
		} else {
			return null;
		}
	}
	
	public long getCombatDuration() {
		return inCombat ? System.currentTimeMillis() - combatStartTime : combatEndTime - combatStartTime;
	}
	
	public void recheckStatus() {
		// Check if combat should end
		int idleMillis = inCombat ? 300 * MinecraftServer.TICK_MS : 100 * MinecraftServer.TICK_MS;
		if (takingDamage && (player.isDead() || System.currentTimeMillis() - lastDamageTime > idleMillis)) {
			boolean wasInCombat = inCombat;
			takingDamage = false;
			inCombat = false;
			combatEndTime = System.currentTimeMillis();
			
			if (wasInCombat) {
				onLeaveCombat();
			}
			
			entries.clear();
		}
	}
	
	public int getKillerId() {
		LivingEntity killer = getKiller();
		return killer == null ? -1 : killer.getEntityId();
	}
	
	public Component getEntityName() {
		return EntityUtils.getName(player);
	}
	
	private void onEnterCombat() {
		player.getPlayerConnection().sendPacket(new EnterCombatEventPacket());
	}
	
	private void onLeaveCombat() {
		int duration = (int) (getCombatDuration() / MinecraftServer.TICK_MS);
		player.getPlayerConnection().sendPacket(new EndCombatEventPacket(duration, getKillerId()));
	}
}

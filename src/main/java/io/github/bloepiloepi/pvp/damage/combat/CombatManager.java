package io.github.bloepiloepi.pvp.damage.combat;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.Tracker;
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
		Block lastClimbedBlock = player.getTag(Tracker.LAST_CLIMBED_BLOCK);
		if (lastClimbedBlock == null) {
			//TODO check for water at feet
			return;
		}
		
		if (lastClimbedBlock.compare(Block.LADDER) || lastClimbedBlock.compare(Block.ACACIA_TRAPDOOR)
				|| lastClimbedBlock.compare(Block.BIRCH_TRAPDOOR) || lastClimbedBlock.compare(Block.CRIMSON_TRAPDOOR)
				|| lastClimbedBlock.compare(Block.IRON_TRAPDOOR) || lastClimbedBlock.compare(Block.DARK_OAK_TRAPDOOR)
				|| lastClimbedBlock.compare(Block.JUNGLE_TRAPDOOR) || lastClimbedBlock.compare(Block.OAK_TRAPDOOR)
				|| lastClimbedBlock.compare(Block.SPRUCE_TRAPDOOR) || lastClimbedBlock.compare(Block.WARPED_TRAPDOOR)) {
			nextFallLocation = "ladder";
			return;
		}
		
		if (lastClimbedBlock.compare(Block.VINE)) {
			nextFallLocation = "vines";
			return;
		}
		
		if (lastClimbedBlock.compare(Block.WEEPING_VINES) || lastClimbedBlock.compare(Block.WEEPING_VINES_PLANT)) {
			nextFallLocation = "weeping_vines";
			return;
		}
		
		if (lastClimbedBlock.compare(Block.TWISTING_VINES) || lastClimbedBlock.compare(Block.TWISTING_VINES_PLANT)) {
			nextFallLocation = "twisting_vines";
			return;
		}
		
		if (lastClimbedBlock.compare(Block.SCAFFOLDING)) {
			nextFallLocation = "scaffolding";
			return;
		}
		
		nextFallLocation = "other_climbable";
	}
	
	public void recordDamage(CustomDamageType damageType, float damage) {
		recheckStatus();
		prepareForDamage();
		
		CombatEntry entry = new CombatEntry(damageType, damage, nextFallLocation, player.getTag(Tracker.FALL_DISTANCE));
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
		if (lastEntry.damageType().isFall()) {
			heaviestFall = getHeaviestFall();
			fall = heaviestFall != null;
		}
		
		if (!fall) {
			return lastEntry.damageType().getDeathMessage(player);
		}
		
		if (heaviestFall.damageType().isFall() || heaviestFall.damageType() == CustomDamageType.OUT_OF_WORLD) {
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
			if (attacker instanceof Player && (player == null || entry.damage() > playerDamage)) {
				player = (Player) attacker;
				playerDamage = entry.damage();
			} else if (attacker instanceof LivingEntity && (entity == null || entry.damage() <= livingDamage)) {
				entity = (LivingEntity) attacker;
				livingDamage = entry.damage();
			}
		}
		
		if (player != null && playerDamage >= livingDamage / 3.0F) {
			return player;
		}
		
		return entity;
	}
	
	public @Nullable CombatEntry getHeaviestFall() {
		CombatEntry mostDamageEntry = null;
		CombatEntry highestFallEntry = null;
		float mostDamage = 0.0F;
		double highestFall = 0.0F;
		
		for (int i = 0; i < entries.size(); i++) {
			CombatEntry entry = entries.get(i);
			
			if ((entry.damageType().isFall() || entry.damageType().isOutOfWorld())
					&& entry.getFallDistance() > 0.0 && (mostDamageEntry == null || entry.getFallDistance() > highestFall)) {
				if (i > 0) {
					mostDamageEntry = entries.get(i - 1);
				} else {
					mostDamageEntry = entry;
				}
				
				highestFall = entry.getFallDistance();
			}
			
			if (entry.fallLocation() != null && (highestFallEntry == null || entry.damage() > mostDamage)) {
				highestFallEntry = entry;
				mostDamage = entry.damage();
			}
		}
		
		if (highestFall > 5.0 && mostDamageEntry != null) {
			return mostDamageEntry;
		} else if (mostDamage > 5.0F) {
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
			reset();
			combatEndTime = System.currentTimeMillis();
		}
	}
	
	public void reset() {
		boolean wasInCombat = inCombat;
		takingDamage = false;
		inCombat = false;
		
		if (wasInCombat) {
			onLeaveCombat();
		}
		
		entries.clear();
	}
	
	public int getKillerId() {
		LivingEntity killer = getKiller();
		return killer == null ? -1 : killer.getEntityId();
	}
	
	public Component getEntityName() {
		return EntityUtils.getName(player);
	}
	
	@SuppressWarnings("UnstableApiUsage")
	private void onEnterCombat() {
		player.getPlayerConnection().sendPacket(new EnterCombatEventPacket());
	}
	
	@SuppressWarnings("UnstableApiUsage")
	private void onLeaveCombat() {
		int duration = (int) (getCombatDuration() / MinecraftServer.TICK_MS);
		player.getPlayerConnection().sendPacket(new EndCombatEventPacket(duration, getKillerId()));
	}
	
	public List<CombatEntry> getEntries() {
		return entries;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public long getLastDamageTime() {
		return lastDamageTime;
	}
	
	public long getCombatStartTime() {
		return combatStartTime;
	}
	
	public long getCombatEndTime() {
		return combatEndTime;
	}
	
	public boolean isInCombat() {
		return inCombat;
	}
	
	public boolean isTakingDamage() {
		return takingDamage;
	}
	
	public String getNextFallLocation() {
		return nextFallLocation;
	}
}

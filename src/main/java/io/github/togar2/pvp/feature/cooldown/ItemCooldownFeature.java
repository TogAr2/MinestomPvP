package io.github.togar2.pvp.feature.cooldown;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

/**
 * Combat feature to manage a players item cooldown animation.
 */
public interface ItemCooldownFeature extends CombatFeature {
	ItemCooldownFeature NO_OP = new ItemCooldownFeature() {
		@Override
		public boolean hasCooldown(Player player, String cooldownGroup) {
			return false;
		}

        @Override
        public boolean hasCooldown(Player player, ItemStack itemStack) {
            return false;
        }

        @Override
		public void setCooldown(Player player, String cooldownGroup, int ticks) {}

        @Override
        public void setCooldown(Player player, ItemStack itemStack, int ticks) {}

        @Override
        public void setCooldown(Player player, ItemStack itemStack) {}
    };

    /**
     *                      Checks if a player has a cooldown group on cooldown
     * @param player        The player to check
     * @param cooldownGroup The cooldown group, usually from the item's {@link net.minestom.server.item.component.UseCooldown}
     *                      component or the item's material
     * @return              {@code true} if player has cooldown for item's cooldown group, otherwise {@code false}
     */
	boolean hasCooldown(Player player, String cooldownGroup);

    /**
     *                      Checks if a player has an item on cooldown
     * @param player        The player to check
     * @param itemStack     The item, from which the cooldown group is fetched from the item's
     *                      {@link net.minestom.server.item.component.UseCooldown}, otherwise uses the item's
     *                      material if component is null
     * @return              {@code true} if player has cooldown for item's cooldown group, otherwise {@code false}
     */
    boolean hasCooldown(Player player, ItemStack itemStack);

    /**
     *                      Sets the cooldown of a cooldown group for a player
     * @param player        The player to target
     * @param cooldownGroup The cooldown group, usually from the item's {@link net.minestom.server.item.component.UseCooldown}
     *                      component or the item's material
     * @param ticks         The amount of ticks to set the cooldown for
     */
	void setCooldown(Player player, String cooldownGroup, int ticks);

    /**
     *                      Sets the cooldown of an item for a player
     * @param player        The player to target
     * @param itemStack     The item, from which the cooldown group is fetched from the item's
     *                      {@link net.minestom.server.item.component.UseCooldown}, otherwise uses the item's material
     *                      if component is null
     * @param ticks         The amount of ticks to set the cooldown for
     */
    void setCooldown(Player player, ItemStack itemStack, int ticks);

    /**
     *                      Sets the cooldown of an item for a player
     *                      This method calculates the ticks from the item's {@link net.minestom.server.item.component.UseCooldown}
     *                      component, otherwise defaults to 0 ticks (no cooldown) if component is null
     *
     * @param player        The player to target
     * @param itemStack     The item, from which the cooldown group is fetched from the item's
     *                      {@link net.minestom.server.item.component.UseCooldown}, otherwise uses the item's material
     *                      if component is null
     */
    void setCooldown(Player player, ItemStack itemStack);
}

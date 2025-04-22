package com.berttowne.materialchicks.util.item.custom;

import com.berttowne.materialchicks.MaterialChicks;
import com.berttowne.materialchicks.util.TimeFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class representing a custom item
 */
public abstract class CustomItem {
    
    private final MaterialChicks plugin;

    private final String name;
    private final List<CustomItemAction> actions;
    private final Map<UUID, Map<Action, Long>> cooldowns;
    private final long defaultCooldown;
    private final boolean hasCooldown;
    private final NamespacedKey customItemKey;

    /**
     * Constructor for a custom item with a cooldown
     * @param name The name of the custom item
     * @param defaultCooldown The default cooldown in milliseconds
     */
    public CustomItem(final String name, final long defaultCooldown) {
        this.plugin = MaterialChicks.getPlugin(MaterialChicks.class);

        this.name = name;
        this.actions = new ArrayList<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.defaultCooldown = defaultCooldown;
        this.hasCooldown = true;
        this.customItemKey = new NamespacedKey(plugin, "custom_item");
    }

    /**
     * Constructor for a custom item without a cooldown
     * @param name The name of the custom item
     */
    public CustomItem(final String name) {
        this.plugin = MaterialChicks.getPlugin(MaterialChicks.class);

        this.name = name;
        this.actions = new ArrayList<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.defaultCooldown = 0;
        this.hasCooldown = false;
        this.customItemKey = new NamespacedKey(plugin, "custom_item");
    }

    /**
     * Get the name of the custom item
     * @return The name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the list of actions for this custom item
     * @return The list of actions
     */
    public List<CustomItemAction> getActions() {
        return Collections.unmodifiableList(this.actions);
    }

    /**
     * Add an action to this custom item
     * @param action The action to add
     */
    public void registerAction(final CustomItemAction action) {
        this.actions.add(action);
    }

    /**
     * Remove an action from this custom item
     * @param action The action to remove
     */
    public void removeAction(final CustomItemAction action) {
        this.actions.remove(action);
    }

    /**
     * Check if this custom item has a cooldown
     * @return True if the item has a cooldown, false otherwise
     */
    public boolean hasCooldown() {
        return this.hasCooldown;
    }

    /**
     * Check if a player is on cooldown for a specific action type
     * @param player The player to check
     * @param actionType The action type to check
     * @return True if the player is on cooldown, false otherwise
     */
    public boolean isOnCooldown(final Player player, final Action actionType) {
        if (!this.hasCooldown) {
            return false;
        }

        final UUID playerUUID = player.getUniqueId();
        if (!this.cooldowns.containsKey(playerUUID)) {
            return false;
        }

        final Map<Action, Long> playerCooldowns = this.cooldowns.get(playerUUID);
        if (!playerCooldowns.containsKey(actionType)) {
            return false;
        }

        final long lastUsed = playerCooldowns.get(actionType);
        final long currentTime = System.currentTimeMillis();

        return currentTime - lastUsed < this.defaultCooldown;
    }

    /**
     * Set a cooldown for a player for a specific action type
     * @param player The player to set the cooldown for
     * @param actionType The action type to set the cooldown for
     */
    public void setCooldown(final Player player, final Action actionType) {
        if (!this.hasCooldown) {
            return;
        }

        final UUID playerUUID = player.getUniqueId();
        if (!this.cooldowns.containsKey(playerUUID)) {
            this.cooldowns.put(playerUUID, new HashMap<>());
        }

        this.cooldowns.get(playerUUID).put(actionType, System.currentTimeMillis());
    }

    /**
     * Get the remaining cooldown time for a player for a specific action type
     * @param player The player to get the cooldown for
     * @param actionType The action type to get the cooldown for
     * @return The remaining cooldown time in milliseconds
     */
    public long getRemainingCooldown(final Player player, final Action actionType) {
        if (!this.hasCooldown) {
            return 0;
        }

        final UUID playerUUID = player.getUniqueId();
        if (!this.cooldowns.containsKey(playerUUID)) {
            return 0;
        }

        final Map<Action, Long> playerCooldowns = this.cooldowns.get(playerUUID);
        if (!playerCooldowns.containsKey(actionType)) {
            return 0;
        }

        final long lastUsed = playerCooldowns.get(actionType);
        final long currentTime = System.currentTimeMillis();
        final long elapsed = currentTime - lastUsed;

        return Math.max(0, this.defaultCooldown - elapsed);
    }

    /**
     * Create an ItemStack for this custom item with the given data
     * @param data The data to apply to the item
     * @return The created ItemStack
     */
    public ItemStack getItemStack(final String... data) throws CustomItemException {
        final ItemStack itemStack = this.createItemStack(data);
        return this.applyCustomItemData(itemStack);
    }

    /**
     * Create an ItemStack for this custom item with the given data.
     * This method should be implemented by subclasses to define the specific item creation logic,
     * but should NOT be called directly. Instead, use {@link #getItemStack(String...)}.
     *
     * @param data The data to apply to the item
     * @return The created ItemStack
     */
    protected abstract ItemStack createItemStack(final String... data) throws CustomItemException;

    /**
     * Get tab completions for the data parameter
     * @param currentArgs The current arguments being typed
     * @return A list of possible tab completions
     */
    public abstract List<String> getTabCompletions(final List<String> currentArgs);

    /**
     * Handle a player interaction with this custom item
     * @param player The player who interacted
     * @param event The interaction event
     * @param actionType The type of action
     */
    public void handleInteraction(final Player player, final PlayerInteractEvent event, final Action actionType) {
        if (this.hasCooldown && this.isOnCooldown(player, actionType)) {
            final long remaining = this.getRemainingCooldown(player, actionType);
            player.sendActionBar(Component.text("This item is on cooldown for ").color(NamedTextColor.RED)
                    .append(Component.text(TimeFormatter.formatTimeDifference(remaining, false)).color(NamedTextColor.WHITE)));

            return;
        }

        for (final CustomItemAction action : this.actions) {
            for (final Action type : action.getType()) {
                if (type == actionType) {
                    action.execute(player, event.getItem(), event);

                    if (this.hasCooldown) {
                        this.setCooldown(player, actionType);
                    }
                }
            }
        }
    }

    /**
     * Handle a player placing this custom item as a block
     * @param player The player who placed the block
     * @param event The block place event
     * @return True if the placement should be allowed, false otherwise
     */
    public boolean handlePlacement(final Player player, final BlockPlaceEvent event) {
        // Default implementation allows placement
        return true;
    }

    /**
     * Apply custom item data to an ItemStack
     * @param itemStack The ItemStack to apply data to
     * @return The ItemStack with applied data
     */
    protected ItemStack applyCustomItemData(final @NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(this.customItemKey, PersistentDataType.STRING, this.name);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Check if an ItemStack is this custom item
     * @param itemStack The ItemStack to check
     * @return True if the ItemStack is this custom item, false otherwise
     */
    public boolean isCustomItem(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(this.customItemKey, PersistentDataType.STRING)) {
            return false;
        }

        final String customItemName = container.get(this.customItemKey, PersistentDataType.STRING);
        return this.name.equals(customItemName);
    }

    /**
     * Take one item from the player's specified slot
     * @param player The player whose hand to take from
     * @param slot The slot to set the item to
     * @param itemStack The ItemStack to set
     */
    public void takeAndSetSlot(Player player, EquipmentSlot slot, @NotNull ItemStack itemStack) {
        int hand = itemStack.getAmount() - 1;

        if (hand > 0) {
            itemStack.setAmount(hand);
            player.getInventory().setItem(slot, itemStack);
        } else {
            player.getInventory().setItem(slot, null);
        }

        player.updateInventory();
    }

}
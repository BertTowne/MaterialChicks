package com.berttowne.materialchicks.util.item.custom;

import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Listener for custom item interactions
 */
@Singleton
@AutoService(Listener.class)
public class CustomItemListener implements Listener {

    @Inject private CustomItemService customItemService;

    /**
     * Handle player interactions with custom items
     * @param event The interaction event
     */
    @EventHandler
    public void onPlayerInteract(final @NotNull PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack itemStack = event.getItem();

        if (itemStack == null) return;

        final CustomItem customItem = customItemService.getCustomItem(itemStack);

        if (customItem == null) return;

        // Get the action type
        final Action action = event.getAction();

        // Handle the interaction
        customItem.handleInteraction(player, event, action);

        // Prevent default interaction if the event was used
        if (event.useItemInHand() == Event.Result.DENY) {
            event.setCancelled(true);
        }
    }

    /**
     * Handle block placement for custom items
     * @param event The block place event
     */
    @EventHandler
    public void onBlockPlace(final @NotNull BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final ItemStack itemStack = event.getItemInHand();

        final CustomItem customItem = customItemService.getCustomItem(itemStack);

        if (customItem == null) return;

        // Let the custom item handle the placement
        final boolean allowPlacement = customItem.handlePlacement(player, event);
        if (!allowPlacement) {
            event.setCancelled(true);
        }
    }

}
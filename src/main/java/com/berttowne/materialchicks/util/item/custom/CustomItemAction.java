package com.berttowne.materialchicks.util.item.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Interface representing an action that can be performed with a custom item
 */
public interface CustomItemAction {

    /**
     * Get the types of this action
     * @return The action types
     */
    List<Action> getType();

    /**
     * Execute this action
     * @param player The player who triggered the action
     * @param item The item that triggered the action
     * @param event The interaction event
     */
    void execute(Player player, ItemStack item, PlayerInteractEvent event);

}
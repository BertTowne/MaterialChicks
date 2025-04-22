package com.berttowne.materialchicks.util.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public record GUIIcon(ItemStack itemStack, Map<Set<ClickType>, BiFunction<Player, Integer, String>> clickFunctions) {

    public @Nullable BiFunction<Player, Integer, String> getClickFunction(ClickType action) {
        for (Set<ClickType> actions : clickFunctions().keySet()) {
            if (actions.contains(action)) return clickFunctions().get(actions);
        }

        return null;
    }

}
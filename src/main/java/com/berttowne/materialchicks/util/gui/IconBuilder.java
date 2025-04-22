package com.berttowne.materialchicks.util.gui;

import com.google.common.collect.Sets;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public class IconBuilder {

    private final ItemStack itemStack;
    private final Map<Set<ClickType>, BiFunction<Player, Integer, String>> clickFunctions;

    public IconBuilder(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.clickFunctions = new HashMap<>();
    }

    public IconBuilder(@Nonnull ItemStack itemStack, @Nonnull Map<Set<ClickType>, BiFunction<Player, Integer, String>> clickFunctions) {
        this.itemStack = itemStack;
        this.clickFunctions = clickFunctions;
    }

    public IconBuilder(@Nonnull GUIIcon guiIcon) {
        this.itemStack = guiIcon.itemStack();
        this.clickFunctions = guiIcon.clickFunctions();
    }

    public IconBuilder onLeftClick(@Nonnull BiFunction<Player, Integer, String> clickFunction, boolean includeShift) {
        if (includeShift) {
            return onCustomClick(clickFunction, ClickType.LEFT, ClickType.SHIFT_LEFT, ClickType.DOUBLE_CLICK);
        } else {
            return onCustomClick(clickFunction, ClickType.LEFT, ClickType.DOUBLE_CLICK);
        }
    }

    public IconBuilder onRightClick(@Nonnull BiFunction<Player, Integer, String> clickFunction, boolean includeShift) {
        if (includeShift) {
            return onCustomClick(clickFunction, ClickType.RIGHT, ClickType.SHIFT_RIGHT);
        } else {
            return onCustomClick(clickFunction, ClickType.RIGHT);
        }
    }

    public IconBuilder onShiftClick(@Nonnull BiFunction<Player, Integer, String> clickFunction) {
        return onCustomClick(clickFunction, ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT);
    }

    public IconBuilder onKeyboardClick(@Nonnull BiFunction<Player, Integer, String> clickFunction) {
        return onCustomClick(clickFunction, ClickType.NUMBER_KEY, ClickType.DROP, ClickType.CONTROL_DROP);
    }

    public IconBuilder onCreativeClick(@Nonnull BiFunction<Player, Integer, String> clickFunction) {
        return onCustomClick(clickFunction, ClickType.CREATIVE, ClickType.MIDDLE);
    }

    public IconBuilder onAnyClick(@Nonnull BiFunction<Player, Integer, String> clickFunction) {
        if (!clickFunctions.isEmpty())
            throw new UnsupportedOperationException("An icon is only able to have one function per ClickType!");

        clickFunctions.put(Sets.newHashSet(ClickType.values()), clickFunction);

        return this;
    }

    public IconBuilder onCustomClick(@Nonnull BiFunction<Player, Integer, String> clickFunction, @Nonnull ClickType... clickTypes) {
        for (Set<ClickType> actions : clickFunctions.keySet()) {
            for (ClickType clickType : clickTypes) {
                if (actions.contains(clickType))
                    throw new UnsupportedOperationException("An icon is only able to have one function per ClickType! Conflicting: " + clickType.name());
            }
        }

        clickFunctions.put(Sets.newHashSet(clickTypes), clickFunction);

        return this;
    }

    public GUIIcon build() {
        return new GUIIcon(itemStack, clickFunctions);
    }

}
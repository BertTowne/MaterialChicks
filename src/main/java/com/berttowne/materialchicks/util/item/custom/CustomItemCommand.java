package com.berttowne.materialchicks.util.item.custom;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Command for giving custom items to players
 */
@SuppressWarnings("UnstableApiUsage")
public class CustomItemCommand implements BasicCommand {

    private CustomItemService customItemService;

    public CustomItemCommand(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String[] args) {
        if (!(commandSourceStack.getExecutor() instanceof Player sender)) {
            commandSourceStack.getExecutor().sendMessage(Component.text("This command can only be used by players").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /customitem <custom item name> <player> [data...]").color(NamedTextColor.RED));
        }

        final String customItemName = args[0];
        final String playerName = args[1];

        // Get the custom item
        final CustomItem customItem = customItemService.getCustomItem(customItemName);
        if (customItem == null) {
            sender.sendMessage(Component.text("Custom item not found: " + customItemName).color(NamedTextColor.RED));
            return;
        }

        // Get the player
        final Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player not found: " + playerName).color(NamedTextColor.RED));
            return;
        }

        // Get the data
        final String[] data = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[0];

        // Create the item stack
        final ItemStack itemStack;
        try {
            itemStack = customItem.getItemStack(data);

            // Give the item to the player
            targetPlayer.getInventory().addItem(itemStack);

            sender.sendMessage(Component.text("Gave " + customItemName + " to " + playerName).color(NamedTextColor.GREEN));
        } catch (CustomItemException e) {
            sender.sendMessage(Component.text("Error creating item: " + e.getMessage()).color(NamedTextColor.RED));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String @NotNull [] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 0) {
            // Tab complete custom item names
            completions.addAll(customItemService.getAllCustomItems().stream()
                    .map(CustomItem::getName)
                    .toList());
        } else if (args.length == 1) {
            // Tab complete custom item names
            final String partialName = args[0].toLowerCase();
            completions.addAll(customItemService.getAllCustomItems().stream()
                    .map(CustomItem::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .toList());
        } else if (args.length == 2) {
            // Tab complete player names
            final String partialName = args[1].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .toList());
        } else {
            // Tab complete custom item data
            final String customItemName = args[0];
            final CustomItem customItem = customItemService.getCustomItem(customItemName);

            if (customItem != null) {
                completions.addAll(customItem.getTabCompletions(Arrays.asList(args).subList(2, args.length)));
            }
        }

        return completions;
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender instanceof Player || sender.hasPermission("neoskyblock.customitem");
    }

    @Override
    public @Nullable String permission() {
        return "neoskyblock.customitem";
    }

}
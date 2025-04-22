package com.berttowne.materialchicks.util.item.custom;

import com.berttowne.materialchicks.MaterialChicks;
import com.berttowne.materialchicks.util.injection.GuiceServiceLoader;
import com.berttowne.materialchicks.util.injection.Service;
import com.google.auto.service.AutoService;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manager for custom items
 */
@Singleton
@AutoService(Service.class)
public class CustomItemService implements Service {

    @Inject private Logger logger;
    @Inject private MaterialChicks plugin;

    private final Map<String, CustomItem> customItems = Maps.newConcurrentMap();

    /**
     * Initialize the service
     */
    @Override
    public void onEnable() {
        logger.info("Registering Custom Items...");
        GuiceServiceLoader.load(CustomItem.class, plugin.getClass().getClassLoader()).forEach(this::registerCustomItem);
    }

    /**
     * Register a custom item
     * @param customItem The custom item to register
     */
    public void registerCustomItem(final CustomItem customItem) {
        this.customItems.put(customItem.getName().toLowerCase(), customItem);
    }

    /**
     * Unregister a custom item
     * @param name The name of the custom item to unregister
     */
    public void unregisterCustomItem(final @NotNull String name) {
        this.customItems.remove(name.toLowerCase());
    }

    /**
     * Get a custom item by name
     * @param name The name of the custom item
     * @return The custom item, or null if not found
     */
    public CustomItem getCustomItem(final @NotNull String name) {
        return this.customItems.get(name.toLowerCase());
    }

    /**
     * Get the custom item from an ItemStack
     * @param itemStack The ItemStack to check
     * @return The custom item, or null if the ItemStack is not a custom item
     */
    public CustomItem getCustomItem(final ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        for (final CustomItem customItem : this.customItems.values()) {
            if (customItem.isCustomItem(itemStack)) {
                return customItem;
            }
        }

        return null;
    }

    /**
     * Get a custom item by class
     * @param customItemClass The class of the custom item
     * @return The custom item, or null if not found
     */
    public CustomItem getCustomItem(final Class<? extends CustomItem> customItemClass) {
        for (final CustomItem customItem : this.customItems.values()) {
            if (customItem.getClass().equals(customItemClass)) {
                return customItem;
            }
        }

        return null;
    }

    /**
     * Get all registered custom items
     * @return An unmodifiable collection of all custom items
     */
    public Collection<CustomItem> getAllCustomItems() {
        return Collections.unmodifiableCollection(this.customItems.values());
    }

}
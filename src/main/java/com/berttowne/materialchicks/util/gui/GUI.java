package com.berttowne.materialchicks.util.gui;

import com.berttowne.materialchicks.MaterialChicks;
import com.berttowne.materialchicks.util.Scheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 *
 */
@SuppressWarnings({"deprecation", "unused"})
public abstract class GUI {

    protected static final ItemStack SPACER = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);

    static {
        ItemMeta spacerMeta = SPACER.getItemMeta();
        Objects.requireNonNull(spacerMeta).setDisplayName(ChatColor.RESET + "");
        SPACER.setItemMeta(spacerMeta);
    }

    private final MaterialChicks plugin = MaterialChicks.getPlugin(MaterialChicks.class);

    protected InventoryProxy inventory;
    protected Player player = null;
    protected boolean populated = false;
    private final Inventory bukkitInventory;
    private boolean invCheckOverride = false;
    private boolean allowDrag = false;
    private boolean allowShiftClicking = false;
    private ScheduledTask updaterTask;

    private final Map<Integer, ErrorIcon> errorIconMap = new HashMap<>();
    private final Map<Integer, GUIIcon> guiIconMap = new HashMap<>();

    public GUI(Component title, int size) {
        this.bukkitInventory = Bukkit.createInventory(null, getInvSizeForCount(size), title);
        this.inventory = new InventoryProxy(bukkitInventory, Bukkit.createInventory(null, getInvSizeForCount(size), title));

        Bukkit.getPluginManager().registerEvents(new GUIEvents(this), this.plugin);
    }

    public GUI(Inventory bukkitInventory) {
        this.bukkitInventory = bukkitInventory;
        this.inventory = new InventoryProxy(bukkitInventory, bukkitInventory);

        Bukkit.getPluginManager().registerEvents(new GUIEvents(this), this.plugin);
    }

    @Deprecated
    public GUI(String title, InventoryType type) {
        this.bukkitInventory = Bukkit.createInventory(null, type, title);
        this.inventory = new InventoryProxy(bukkitInventory, Bukkit.createInventory(null, type, title));

        Bukkit.getPluginManager().registerEvents(new GUIEvents(this), this.plugin);
    }

    @SuppressWarnings("UnusedReturnValue")
    public InventoryView open(Player p) {
        this.player = p;

        try {
            if (!this.populated) {
                this.populate();
                this.inventory.apply();
                this.populated = true;
            }

            return this.openInventory(p);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final void close() {
        for (HumanEntity human : new ArrayList<>(this.getBukkitInventory().getViewers())) {
            human.closeInventory();
        }
    }

    protected InventoryView openInventory(@Nonnull Player p) {
        return p.openInventory(this.getBukkitInventory());
    }

    public Inventory getBukkitInventory() {
        return bukkitInventory;
    }

    public Inventory getProxyInventory() {
        return inventory;
    }

    protected abstract void onGUIInventoryClick(InventoryClickEvent event);

    protected void onPlayerInventoryClick(InventoryClickEvent event) {
    }

    protected void onTickUpdate() {
    }

    protected void onPlayerCloseInv(InventoryCloseEvent event) {
    }

    protected void onPlayerDrag(InventoryDragEvent event) {
    }

    protected final int getInvSizeForCount(int count) {
        int size = count / 9 * 9;

        if (count % 9 > 0) {
            size += 9;
        }

        if (size < 9) {
            return 9;
        }

        return Math.min(size, 54);
    }

    public void setInvCheckOverride(boolean invCheckOverride) {
        this.invCheckOverride = invCheckOverride;
    }

    protected abstract void populate();

    private void cleanupErrors() {
        Iterator<Map.Entry<Integer, ErrorIcon>> iterator = errorIconMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, ErrorIcon> entry = iterator.next();
            ErrorIcon icon = entry.getValue();

            if (System.currentTimeMillis() >= icon.expire()) {
                iterator.remove();
            }
        }
    }

    protected void repopulate() {
        try {
            this.inventory.clear();

            if (player == null || player.isOnline()) {
                this.populate();
                cleanupErrors();

                for (Map.Entry<Integer, GUIIcon> entry : guiIconMap.entrySet()) {
                    int slot = entry.getKey();
                    GUIIcon icon = entry.getValue();

                    this.inventory.setItem(slot, icon.itemStack());
                }

                for (Map.Entry<Integer, ErrorIcon> entry : errorIconMap.entrySet()) {
                    int slot = entry.getKey();
                    ErrorIcon icon = entry.getValue();

                    this.inventory.setItem(slot, icon.toItem());
                }

                this.inventory.apply();
            }

            this.populated = true;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected final void setUpdateTicks(int ticks) {
        this.setUpdateTicks(ticks, false);
    }

    @SuppressWarnings("SameParameterValue")
    protected final void setUpdateTicks(int ticks, boolean sync) {
        if (this.updaterTask != null) {
            this.updaterTask.cancel();
            this.updaterTask = null;
        }

        if (sync) {
            this.updaterTask = Scheduler.repeat(player, new GUIUpdateTask(this), 0, ticks);
        } else {
            this.updaterTask = Scheduler.repeatAsync(new GUIUpdateTask(this), 0, ticks * 50L, TimeUnit.MILLISECONDS);
        }
    }

    public void setSlot(int slot, @Nullable GUIIcon icon) {
        if (icon == null) {
            inventory.setItem(slot, null);
            guiIconMap.remove(slot);
        } else {
            inventory.setItem(slot, icon.itemStack());
            guiIconMap.put(slot, icon);
        }
    }

    public void showError(int slot, String title, String... subtitle) {
        Runnable runnable = () -> {
            ErrorIcon icon = new ErrorIcon(title, subtitle, System.currentTimeMillis() + 3000);
            errorIconMap.put(slot, icon);
            repopulate();
        };

        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Scheduler.run(player, runnable);
        }
    }

    protected final void scheduleOpen(final GUI gui, final Player player) {
        Scheduler.run(player, () -> gui.open(player));
    }

    protected void setAllowDrag(boolean allowDrag) {
        this.allowDrag = allowDrag;
    }

    protected boolean isAllowShiftClicking() {
        return allowShiftClicking;
    }

    protected void setAllowShiftClicking(boolean allowShiftClicking) {
        this.allowShiftClicking = allowShiftClicking;
    }

    protected Map<Integer, GUIIcon> getGuiIconMap() {
        return guiIconMap;
    }

    private class GUIEvents implements Listener {

        private final GUI gui;

        public GUIEvents(GUI gui) {
            this.gui = gui;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            try {
                if (this.gui.bukkitInventory.getViewers().contains(event.getWhoClicked())) {
                    List<InventoryAction> deniedActions = new ArrayList<>(Arrays.asList(
                            InventoryAction.CLONE_STACK,
                            InventoryAction.COLLECT_TO_CURSOR,
                            InventoryAction.UNKNOWN
                    ));

                    if (!allowShiftClicking) {
                        deniedActions.add(InventoryAction.MOVE_TO_OTHER_INVENTORY);
                    }

                    if (deniedActions.contains(event.getAction())) {
                        event.setCancelled(true);
                    }

                    if (!allowShiftClicking && event.getClick().isShiftClick()) {
                        event.setCancelled(true);
                    }

                    if (!invCheckOverride && event.getClickedInventory() == null) {
                        return;
                    }

                    if (!Objects.equals(event.getClickedInventory(), gui.getBukkitInventory())) {
                        gui.onPlayerInventoryClick(event);
                        return;
                    }

                    event.setCancelled(true);

                    if (!(event.getWhoClicked() instanceof Player)) {
                        return;
                    }

                    if (gui.getGuiIconMap().containsKey(event.getRawSlot())) {
                        GUIIcon icon = gui.getGuiIconMap().get(event.getRawSlot());

                        if (icon.getClickFunction(event.getClick()) != null) {
                            String errorMsg = Objects.requireNonNull(icon.getClickFunction(event.getClick())).apply(player, event.getRawSlot());

                            if (errorMsg != null) {
                                showError(event.getRawSlot(), "Uh oh!", WordUtils.wrap(errorMsg, 40).split(System.lineSeparator()));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                            }

                            return; // Stop here to prevent double-firing from legacy system
                        }
                    }

                    gui.onGUIInventoryClick(event);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @EventHandler
        public void onInventoryClose(@Nonnull InventoryCloseEvent event) {
            if (!event.getInventory().equals(gui.getBukkitInventory())) {
                return;
            }

            if (bukkitInventory.getViewers().size() <= 1) {
                HandlerList.unregisterAll(this);

                try {
                    gui.onPlayerCloseInv(event);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }

                if (gui.updaterTask != null) {
                    gui.updaterTask.cancel();
                }
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            try {
                if (!event.getInventory().equals(gui.getBukkitInventory())) {
                    return;
                }

                if (!allowDrag) {
                    event.setCancelled(true);
                } else {
                    gui.onPlayerDrag(event);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class GUIUpdateTask implements Consumer<ScheduledTask> {

        private final GUI gui;

        public GUIUpdateTask(GUI gui) {
            this.gui = gui;
        }

        @Override
        public void accept(ScheduledTask scheduledTask) {
            try {
                this.gui.repopulate();
                this.gui.onTickUpdate();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public record ErrorIcon(String errorTitle, String[] errorSubtitle, long expire) {

        @Nonnull
        public ItemStack toItem() {
            ItemStack itemStack = new ItemStack(Material.BARRIER);
            ItemMeta itemMeta = itemStack.getItemMeta();
            Objects.requireNonNull(itemMeta).setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + errorTitle);

            List<String> lore = new ArrayList<>();
            for (String line : errorSubtitle) {
                lore.add(ChatColor.WHITE + line);
            }

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

    public static class InventoryProxy implements Inventory {

        private final Inventory mainInventory;
        private final Inventory proxyInventory;

        private InventoryProxy(Inventory mainInventory, Inventory proxyInventory) {
            this.mainInventory = mainInventory;
            this.proxyInventory = proxyInventory;
        }

        public void apply() {
            this.mainInventory.setContents(this.proxyInventory.getContents());
        }

        @Override
        public int getSize() {
            return proxyInventory.getSize();
        }

        @Override
        public int getMaxStackSize() {
            return proxyInventory.getMaxStackSize();
        }

        @Override
        public void setMaxStackSize(int i) {
            proxyInventory.setMaxStackSize(i);
        }

        @Override
        public ItemStack getItem(int i) {
            return proxyInventory.getItem(i);
        }

        @Override
        public void setItem(int i, ItemStack itemStack) {
            proxyInventory.setItem(i, itemStack);
        }

        @Override
        @Nonnull
        public HashMap<Integer, ItemStack> addItem(@Nonnull ItemStack... itemStacks) throws IllegalArgumentException {
            return proxyInventory.addItem(itemStacks);
        }

        @Override
        @Nonnull
        public HashMap<Integer, ItemStack> removeItem(@Nonnull ItemStack... itemStacks) throws IllegalArgumentException {
            return proxyInventory.removeItem(itemStacks);
        }

        @Override
        public @NotNull HashMap<Integer, ItemStack> removeItemAnySlot(@Nonnull @NotNull ItemStack... itemStacks) throws IllegalArgumentException {
            return proxyInventory.removeItemAnySlot(itemStacks);
        }

        @Override
        @Nonnull
        public ItemStack[] getContents() {
            return proxyInventory.getContents();
        }

        @Override
        public void setContents(@Nonnull ItemStack[] itemStacks) throws IllegalArgumentException {
            proxyInventory.setContents(itemStacks);
        }

        @Override
        @Nonnull
        public ItemStack[] getStorageContents() {
            return proxyInventory.getContents();
        }

        @Override
        public void setStorageContents(@Nonnull ItemStack[] itemStacks) throws IllegalArgumentException {
            proxyInventory.setContents(itemStacks);
        }

        @Override
        public boolean contains(@Nonnull Material material) throws IllegalArgumentException {
            return proxyInventory.contains(material);
        }

        @Override
        public boolean contains(ItemStack itemStack) {
            return proxyInventory.contains(itemStack);
        }

        @Override
        public boolean contains(@Nonnull Material material, int i) throws IllegalArgumentException {
            return proxyInventory.contains(material, i);
        }

        @Override
        public boolean contains(ItemStack itemStack, int i) {
            return proxyInventory.contains(itemStack, i);
        }

        @Override
        public boolean containsAtLeast(ItemStack itemStack, int i) {
            return proxyInventory.containsAtLeast(itemStack, i);
        }

        @Override
        @Nonnull
        public HashMap<Integer, ? extends ItemStack> all(@Nonnull Material material) throws IllegalArgumentException {
            return proxyInventory.all(material);
        }

        @Override
        @Nonnull
        public HashMap<Integer, ? extends ItemStack> all(ItemStack itemStack) {
            return proxyInventory.all(itemStack);
        }

        @Override
        public int first(@Nonnull Material material) throws IllegalArgumentException {
            return proxyInventory.first(material);
        }

        @Override
        public int first(@Nonnull ItemStack itemStack) {
            return proxyInventory.first(itemStack);
        }

        @Override
        public int firstEmpty() {
            return proxyInventory.firstEmpty();
        }

        @Override
        public boolean isEmpty() {
            return proxyInventory.isEmpty();
        }

        @Override
        public void remove(@Nonnull Material material) throws IllegalArgumentException {
            proxyInventory.remove(material);
        }

        @Override
        public void remove(@Nonnull ItemStack itemStack) {
            proxyInventory.remove(itemStack);
        }

        @Override
        public void clear(int i) {
            proxyInventory.clear(i);
        }

        @Override
        public void clear() {
            proxyInventory.clear();
        }

        @Override
        public int close() {
            return 0;
        }

        @Override
        @Nonnull
        public List<HumanEntity> getViewers() {
            return mainInventory.getViewers();
        }

        @Override
        @Nonnull
        public InventoryType getType() {
            return mainInventory.getType();
        }

        @Override
        public InventoryHolder getHolder() {
            return mainInventory.getHolder();
        }

        @Override
        public InventoryHolder getHolder(boolean b) {
            return proxyInventory.getHolder(b);
        }

        @Override
        @Nonnull
        public ListIterator<ItemStack> iterator() {
            return proxyInventory.iterator();
        }

        @Override
        @Nonnull
        public ListIterator<ItemStack> iterator(int i) {
            return proxyInventory.iterator(i);
        }

        @Override
        public Location getLocation() {
            return proxyInventory.getLocation();
        }
    }

}
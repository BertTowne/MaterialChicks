package com.berttowne.materialchicks.chickens;

import com.berttowne.materialchicks.MaterialChicks;
import com.berttowne.materialchicks.util.item.ItemBuilder;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.Objects;

public class Chicken {

    private final org.bukkit.entity.Chicken.Variant variant;
    private final String chickenType;

    private int growth;
    private int gain;
    private int strength;
    private Instant nextLay;

    public Chicken(String chickenType, int growth, int gain, int strength, Instant nextLay) {
        this.chickenType = chickenType;
        this.variant = RegistryAccess.registryAccess().getRegistry(RegistryKey.CHICKEN_VARIANT)
                .getOrThrow(NamespacedKey.fromString(chickenType + "_chicken", MaterialChicks.getPlugin(MaterialChicks.class)));
        this.growth = growth;
        this.gain = gain;
        this.strength = strength;
        this.nextLay = nextLay;
    }

    public String getChickenType() {
        return chickenType;
    }

    public int getGrowth() {
        return growth;
    }

    public void setGrowth(int growth) {
        this.growth = growth;
    }

    public int getGain() {
        return gain;
    }

    public void setGain(int gain) {
        this.gain = gain;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public Instant getNextLay() {
        return nextLay;
    }

    public void setNextLay(Instant nextLay) {
        this.nextLay = nextLay;
    }

    @SuppressWarnings("deprecation")
    public ItemStack toItemStack() {
        return new ItemBuilder(Material.PAPER)
                .customModelData(chickenType + "_chicken")
                .displayName(Component.text(WordUtils.capitalizeFully(chickenType) + " Chicken", NamedTextColor.YELLOW))
                .addLore(
                        Component.text(" "),
                        Component.text("Growth: ", NamedTextColor.WHITE).append(Component.text(growth, NamedTextColor.GRAY)),
                        Component.text("Gain: ", NamedTextColor.WHITE).append(Component.text(gain, NamedTextColor.GRAY)),
                        Component.text("Strength: ", NamedTextColor.WHITE).append(Component.text(strength, NamedTextColor.GRAY))
                )
                .build();
    }

    public ItemStack getItemToLay() {
        return new ItemStack(Objects.requireNonNull(Material.matchMaterial(chickenType)));
    }

}
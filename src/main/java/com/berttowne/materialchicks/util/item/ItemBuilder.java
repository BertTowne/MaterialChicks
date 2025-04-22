package com.berttowne.materialchicks.util.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerTextures;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "unused", "PatternValidation"})
public class ItemBuilder {

    private final ItemStack itemStack;

    private Material material;
    @Nonnegative private int amount;
    @Nonnegative @IntRange(from = 1L, to = 99L) private int maxStackSize;

    private Component displayName;
    private List<Component> lore = new ArrayList<>();
    private ItemRarity itemRarity;

    private NamespacedKey itemModel;
    private CustomModelData.Builder customModelData;

    private NamespacedKey tooltipStyle;
    private boolean hideTooltip;

    private Set<ItemFlag> itemFlags = Sets.newHashSet();
    private Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();

    private DamageType damageResistant = null;
    private boolean unbreakable;
    @Nonnegative private Integer damageTaken = null;
    @Nonnegative private Integer maxDamage = null;
    @Nonnegative private Integer repairCost = null;

    private boolean showEnchantGlint;
    private Enchantable enchantability = null;
    private Map<Enchantment, Integer> enchantments = Maps.newHashMap();

    private ArmorTrim armorTrim = null;

    private Color color = null;
    private MusicInstrument instrument = null;
    private Equippable equippable = null;
    private Consumable consumable = null;
    private Tool.Builder tool = null;
    private FoodProperties.Builder foodProperties = null;
    private BannerPatternLayers.Builder bannerPatterns = null;
    private PotionContents.Builder potionContents = null;
    private ResolvableProfile.Builder profile = null;
    private PlayerProfile playerProfile = null;
    private DeathProtection deathProtection = null;

    private boolean glider = false;

    public ItemBuilder(Material material, @Nonnegative int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(EntityType spawnEggType) {
        this(Bukkit.getItemFactory().getSpawnEgg(spawnEggType));
    }

    public ItemBuilder(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.material = itemStack.getType();
        this.amount = itemStack.getAmount();
        this.maxStackSize = itemStack.getMaxStackSize();

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName()) {
                this.displayName = meta.displayName();
            }

            if (meta.hasLore()) {
                this.lore = meta.lore();
            }

            if (meta.hasEnchantable()) {
                this.enchantability = Enchantable.enchantable(meta.getEnchantable());
            }

            if (meta instanceof ArmorMeta armorMeta && armorMeta.hasTrim()) {
                this.armorTrim = armorMeta.getTrim();
            }

            this.unbreakable = meta.isUnbreakable();
            this.enchantments = meta.getEnchants();
            this.itemModel = meta.getItemModel();
            this.tooltipStyle = meta.getTooltipStyle();
            this.hideTooltip = meta.isHideTooltip();
            this.itemRarity = meta.getRarity();
            this.showEnchantGlint = meta.hasEnchantmentGlintOverride();
            this.glider = meta.isGlider();
            this.itemFlags = meta.getItemFlags();
        }

        Integer damageValue = itemStack.getData(DataComponentTypes.DAMAGE);
        if (damageValue != null) this.damageTaken = damageValue;

        Integer maxDamageValue = itemStack.getData(DataComponentTypes.MAX_DAMAGE);
        if (maxDamageValue != null) this.maxDamage = maxDamageValue;

        Integer repairCostValue = itemStack.getData(DataComponentTypes.REPAIR_COST);
        if (repairCostValue != null) this.repairCost = repairCostValue;

        CustomModelData customModelData = itemStack.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (customModelData != null) this.customModelData = CustomModelData.customModelData()
                .addColors(customModelData.colors())
                .addFlags(customModelData.flags())
                .addFloats(customModelData.floats())
                .addStrings(customModelData.strings());

        DyedItemColor dyedItemColor = itemStack.getData(DataComponentTypes.DYED_COLOR);
        if (dyedItemColor != null) this.color = dyedItemColor.color();

        DyeColor dyeColor = itemStack.getData(DataComponentTypes.BASE_COLOR);
        if (dyeColor != null) this.color = dyeColor.getColor();

        MusicInstrument musicInstrument = itemStack.getData(DataComponentTypes.INSTRUMENT);
        if (musicInstrument != null) this.instrument = musicInstrument;

        Consumable consumable = itemStack.getData(DataComponentTypes.CONSUMABLE);
        if (consumable != null) this.consumable = consumable;

        FoodProperties foodProperties = itemStack.getData(DataComponentTypes.FOOD);
        if (foodProperties != null) this.foodProperties = foodProperties.toBuilder();

        Equippable equippable = itemStack.getData(DataComponentTypes.EQUIPPABLE);
        if (equippable != null) this.equippable = equippable;

        BannerPatternLayers bannerPatternLayers = itemStack.getData(DataComponentTypes.BANNER_PATTERNS);
        if (bannerPatternLayers != null) {
            this.bannerPatterns = BannerPatternLayers.bannerPatternLayers().addAll(bannerPatternLayers.patterns());
        } else if (itemStack.getType().name().endsWith("BANNER") || itemStack.getType() == Material.SHIELD) {
            this.bannerPatterns = BannerPatternLayers.bannerPatternLayers();
        }

        PotionContents potionContents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        if (potionContents != null) this.potionContents = PotionContents.potionContents()
                .potion(potionContents.potion())
                .customColor(potionContents.customColor())
                .customName(potionContents.customName())
                .addCustomEffects(potionContents.customEffects());

        DamageResistant damageResistant = itemStack.getData(DataComponentTypes.DAMAGE_RESISTANT);
        if (damageResistant != null) this.damageResistant = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.DAMAGE_TYPE)
                .getOrThrow(NamespacedKey.fromString(damageResistant.types().key().asString()));

        Tool tool = itemStack.getData(DataComponentTypes.TOOL);
        if (tool != null) this.tool = Tool.tool()
                .defaultMiningSpeed(tool.defaultMiningSpeed())
                .damagePerBlock(tool.damagePerBlock())
                .addRules(tool.rules());

        ItemAttributeModifiers itemAttributeModifiers = itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (itemAttributeModifiers != null)
            itemAttributeModifiers.modifiers().forEach(entry -> this.attributeModifiers.put(entry.attribute(), entry.modifier()));

        ResolvableProfile profile = itemStack.getData(DataComponentTypes.PROFILE);
        if (profile != null) {
            this.profile = ResolvableProfile.resolvableProfile().name(profile.name())
                    .uuid(profile.uuid())
                    .addProperties(profile.properties());
        }

        DeathProtection deathProtection = itemStack.getData(DataComponentTypes.DEATH_PROTECTION);
        if (deathProtection != null) this.deathProtection = deathProtection;
    }

    public ItemBuilder material(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder amount(@Nonnegative int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder maxStackSize(@Nonnegative @IntRange(from = 1L, to = 99L) int maxStackSize) {
        this.maxStackSize = maxStackSize;
        return this;
    }

    public ItemBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder lore(Component... lore) {
        this.lore = List.of(lore);
        return this;
    }

    public ItemBuilder addLore(Component... lore) {
        this.lore.addAll(List.of(lore));
        return this;
    }

    public ItemBuilder addLore(List<Component> lore) {
        this.lore.addAll(lore);
        return this;
    }

    public ItemBuilder clearLore() {
        this.lore.clear();
        return this;
    }

    public ItemBuilder itemRarity(ItemRarity itemRarity) {
        this.itemRarity = itemRarity;
        return this;
    }

    public ItemBuilder itemModel(NamespacedKey itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public ItemBuilder customModelData(float customModelData) {
        if (this.customModelData == null) this.customModelData = CustomModelData.customModelData();

        this.customModelData.addFloat(customModelData);
        return this;
    }

    public ItemBuilder customModelData(boolean customModelData) {
        if (this.customModelData == null) this.customModelData = CustomModelData.customModelData();

        this.customModelData.addFlag(customModelData);
        return this;
    }

    public ItemBuilder customModelData(String customModelData) {
        if (this.customModelData == null) this.customModelData = CustomModelData.customModelData();

        this.customModelData.addString(customModelData);
        return this;
    }

    public ItemBuilder customModelData(Color customModelData) {
        if (this.customModelData == null) this.customModelData = CustomModelData.customModelData();

        this.customModelData.addColor(customModelData);
        return this;
    }

    public ItemBuilder tooltipStyle(NamespacedKey tooltipStyle) {
        this.tooltipStyle = tooltipStyle;
        return this;
    }

    public ItemBuilder hideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
        return this;
    }

    public ItemBuilder itemFlags(ItemFlag... itemFlags) {
        this.itemFlags.addAll(Set.of(itemFlags));
        return this;
    }

    public ItemBuilder damageResistant(DamageType damageType) {
        this.damageResistant = damageType;
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder damageTaken(@Nonnegative int damageTaken) {
        this.damageTaken = damageTaken;
        return this;
    }

    public ItemBuilder maxDamage(@Nonnegative int maxDamage) {
        this.maxDamage = maxDamage;
        return this;
    }

    public ItemBuilder repairCost(@Nonnegative int repairCost) {
        this.repairCost = repairCost;
        return this;
    }

    public ItemBuilder equippable(Equippable equippable) {
        this.equippable = equippable;
        return this;
    }

    public ItemBuilder equippable(@NotNull EquipmentSlot slot, @NotNull Sound equipSound, @Nullable Key model,
                                  @Nullable Key cameraOverlay, boolean dispensable, boolean swappable,
                                  boolean damageItemOnWearerDamage, EntityType... allowedEntities) {
        this.equippable = Equippable.equippable(slot)
                .equipSound(equipSound.name())
                .assetId(model)
                .cameraOverlay(cameraOverlay)
                .dispensable(dispensable)
                .swappable(swappable)
                .damageOnHurt(damageItemOnWearerDamage)
                .allowedEntities(RegistrySet.keySet(RegistryKey.ENTITY_TYPE,
                        Arrays.stream(allowedEntities)
                        .map(entityType -> TypedKey.create(RegistryKey.ENTITY_TYPE, entityType.getKey()))
                        .collect(Collectors.toList())))
                .build();

        return this;
    }

    public ItemBuilder equippable(@NotNull EquipmentSlot slot) {
        return this.equippable(slot, Sound.sound(Key.key("minecraft", "item.armor.equip"), Sound.Source.MASTER, 1.0f, 1.0f),
                null, null, true, true, true, EntityType.values());
    }

    public ItemBuilder tool(float defaultMiningSpeed, int damagePerBlock, Tool.Rule... rules) {
        this.tool = Tool.tool()
                .defaultMiningSpeed(defaultMiningSpeed)
                .damagePerBlock(damagePerBlock)
                .addRules(List.of(rules));
        return this;
    }

    public ItemBuilder defaultMiningSpeed(float defaultMiningSpeed) {
        if (this.tool == null) this.tool = Tool.tool();

        this.tool.defaultMiningSpeed(defaultMiningSpeed);

        return this;
    }

    public ItemBuilder damagePerBlock(int damagePerBlock) {
        if (this.tool == null) this.tool = Tool.tool();

        this.tool.damagePerBlock(damagePerBlock);

        return this;
    }

    public ItemBuilder addRules(Tool.Rule... rules) {
        if (this.tool == null) this.tool = Tool.tool();

        this.tool.addRules(List.of(rules));

        return this;
    }

    public ItemBuilder addRule(Tool.Rule rule) {
        if (this.tool == null) this.tool = Tool.tool();

        this.tool.addRule(rule);

        return this;
    }

    public ItemBuilder showEnchantGlint(boolean showEnchantGlint) {
        this.showEnchantGlint = showEnchantGlint;
        return this;
    }

    public ItemBuilder enchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return this.enchant(enchantment, enchantment.getStartLevel());
    }

    public ItemBuilder enchantability(@Nonnegative int enchantability) {
        this.enchantability = Enchantable.enchantable(enchantability);
        return this;
    }

    public ItemBuilder glider(boolean glider) {
        this.glider = glider;
        return this;
    }

    public ItemBuilder attributeModifiers(Map<Attribute, AttributeModifier> attributeModifiers) {
        this.attributeModifiers = attributeModifiers;
        return this;
    }

    public ItemBuilder clearAttributeModifiers() {
        this.attributeModifiers.clear();
        return this;
    }

    public ItemBuilder attributeModifier(Attribute attribute, AttributeModifier modifier) {
        this.attributeModifiers.put(attribute, modifier);
        return this;
    }

    public ItemBuilder attributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation) {
        this.attributeModifiers.put(attribute, new AttributeModifier(attribute.getKey(), amount, operation));
        return this;
    }

    public ItemBuilder consumable(Consumable consumable) {
        this.consumable = consumable;
        return this;
    }

    public ItemBuilder consumable(@Nonnegative int consumeSeconds, ItemUseAnimation animation, @NotNull Sound sound,
                                  boolean hasConsumeParticles, ConsumeEffect... consumeEffects) {
        Consumable.Builder consumable = Consumable.consumable();

        this.consumable = consumable.consumeSeconds(consumeSeconds)
                .animation(animation)
                .sound(sound.name())
                .hasConsumeParticles(hasConsumeParticles)
                .addEffects(List.of(consumeEffects))
                .build();

        return this;
    }

    public ItemBuilder consumable() {
        this.consumable = Consumable.consumable()
                .consumeSeconds(1.6f)
                .animation(ItemUseAnimation.EAT)
                .sound(Key.key("minecraft", "entity.generic.eat"))
                .hasConsumeParticles(true)
                .build();
        return this;
    }

    public ItemBuilder foodProperties(@NotNull FoodProperties foodProperties) {
        this.foodProperties = foodProperties.toBuilder();

        if (this.consumable == null) return this.consumable(); // Must be consumable to have food properties, so use default values

        return this;
    }

    public ItemBuilder foodProperties(@Nonnegative int nutrition, @Nonnegative float saturation, boolean canAlwaysEat) {
        if (this.foodProperties == null) this.foodProperties = FoodProperties.food();

        this.foodProperties.nutrition(nutrition).saturation(saturation).canAlwaysEat(canAlwaysEat);

        if (this.consumable == null) return this.consumable(); // Must be consumable to have food properties, so use default values

        return this;
    }

    public ItemBuilder resolvableProfile(@NotNull ResolvableProfile profile) {
        if (this.material != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("Cannot set a resolvable profile on an item that is not a player head!");
        }

        this.profile = ResolvableProfile.resolvableProfile().name(profile.name())
                .uuid(profile.uuid())
                .addProperties(profile.properties());
        return this;
    }

    public ItemBuilder playerSkull(String name) {
        if (this.material != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("Cannot set a player skull on an item that is not a player head!");
        }

        if (this.profile == null) this.profile = ResolvableProfile.resolvableProfile();

        this.profile.name(name);
        return this;
    }

    public ItemBuilder playerSkull(UUID uuid) {
        if (this.material != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("Cannot set a player skull on an item that is not a player head!");
        }

        if (this.profile == null) this.profile = ResolvableProfile.resolvableProfile();

        this.profile.uuid(uuid);
        return this;
    }

    public ItemBuilder playerSkull(URI texture) throws MalformedURLException {
        if (this.material != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("Cannot set a player skull on an item that is not a player head!");
        }

        UUID uuid = UUID.randomUUID();
        this.playerProfile = Bukkit.createProfile(uuid);
        PlayerTextures textures = playerProfile.getTextures();

        textures.setSkin(texture.toURL());
        playerProfile.setTextures(textures);

        return this;
    }

    public ItemBuilder deathProtection(ConsumeEffect... consumeEffects) {
        if (deathProtection == null) {
            deathProtection = DeathProtection.deathProtection(List.of(consumeEffects));
            return this;
        }

        DeathProtection.Builder protection = DeathProtection.deathProtection();
        protection.addEffects(List.of(consumeEffects));
        protection.addEffects(deathProtection.deathEffects());

        this.deathProtection = protection.build();

        return this;
    }

    public ItemBuilder armorTrim(ArmorTrim armorTrim) {
        if (!EnchantmentTarget.ARMOR.includes(this.material)) {
            throw new IllegalArgumentException("Cannot set armor trim on an item that is not armor!");
        }

        this.armorTrim = armorTrim;
        return this;
    }

    public ItemBuilder withColor(Color color) {
        if ((!EnchantmentTarget.ARMOR.includes(this.material) || !this.material.name().contains("LEATHER"))
                && this.material != Material.SHIELD && !this.material.name().endsWith("POTION"))
            throw new IllegalArgumentException("Cannot set color on an item that is not armor, a shield, or a potion!");

        this.color = color;
        return this;
    }

    public ItemBuilder withColor(DyeColor color) {
        if ((!EnchantmentTarget.ARMOR.includes(this.material) || !this.material.name().contains("LEATHER"))
                && this.material != Material.SHIELD)
            throw new IllegalArgumentException("Cannot set color on an item that is not armor or a shield!");

        this.color = color.getColor();
        return this;
    }

    public ItemBuilder withBannerPattern(DyeColor color, PatternType pattern) {
        return this.withBannerPattern(new Pattern(color, pattern));
    }

    public ItemBuilder withBannerPattern(Pattern pattern) {
        if (!itemStack.getType().name().endsWith("BANNER") && itemStack.getType() != Material.SHIELD) {
            throw new IllegalArgumentException("Cannot set banner pattern on an item that is not a banner or a shield!");
        }

        if (this.bannerPatterns == null) {
            this.bannerPatterns = BannerPatternLayers.bannerPatternLayers();
        }

        this.bannerPatterns.add(pattern);

        return this;
    }

    public ItemBuilder withInstrument(MusicInstrument instrument) {
        if (this.material != Material.GOAT_HORN) {
            throw new IllegalArgumentException("Cannot set instrument on an item that is not a goat horn!");
        }

        this.instrument = instrument;
        return this;
    }

    public <P, C> ItemBuilder withPersistentData(String key, PersistentDataType<P, C> dataType, C value) {
        NamespacedKey namespacedKey = new NamespacedKey("hellbounds", key);

        itemStack.editMeta(meta -> meta.getPersistentDataContainer().set(namespacedKey, dataType, value));

        return this;
    }

    public <P, C> ItemBuilder withPersistentData(NamespacedKey key, PersistentDataType<P, C> dataType, C value) {
        itemStack.editMeta(meta -> meta.getPersistentDataContainer().set(key, dataType, value));

        return this;
    }

    public ItemBuilder clearDefaultPotions() {
        if (!itemStack.getType().name().endsWith("POTION") && itemStack.getType() != Material.TIPPED_ARROW) {
            throw new IllegalArgumentException("Cannot clear default potions on an item that is not a potion or tipped arrow!");
        }

        if (this.potionContents == null) {
            this.potionContents = PotionContents.potionContents(); // empty potion contents
            return this;
        }

        PotionContents potionContents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        if (potionContents != null) this.potionContents = PotionContents.potionContents()
                .potion(potionContents.potion())
                .customColor(potionContents.customColor())
                .customName(potionContents.customName());

        return this;
    }

    public ItemBuilder withPotionType(PotionType potionType) {
        if (!itemStack.getType().name().endsWith("POTION") && itemStack.getType() != Material.TIPPED_ARROW) {
            throw new IllegalArgumentException("Cannot set potion type on an item that is not a potion or tipped arrow!");
        }

        if (this.potionContents == null) {
            this.potionContents = PotionContents.potionContents();
        }

        this.potionContents.potion(potionType);

        return this;
    }

    public ItemBuilder withPotionEffect(PotionEffect potionEffect) {
        if (!itemStack.getType().name().endsWith("POTION") && itemStack.getType() != Material.TIPPED_ARROW) {
            throw new IllegalArgumentException("Cannot set potion effect on an item that is not a potion or tipped arrow!");
        }

        if (this.potionContents == null) {
            this.potionContents = PotionContents.potionContents();
        }

        this.potionContents.addCustomEffect(potionEffect);

        return this;
    }

    public ItemBuilder withPotionEffects(PotionEffect... potionEffects) {
        if (!itemStack.getType().name().endsWith("POTION") && itemStack.getType() != Material.TIPPED_ARROW) {
            throw new IllegalArgumentException("Cannot set potion effects on an item that is not a potion or tipped arrow!");
        }

        if (this.potionContents == null) {
            this.potionContents = PotionContents.potionContents();
        }

        this.potionContents.addCustomEffects(List.of(potionEffects));

        return this;
    }

    public ItemStack build() {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            if (displayName != null)    meta.displayName(displayName);
            if (lore != null)           meta.lore(lore);
            if (enchantability != null) meta.setEnchantable(enchantability.value());
            if (itemModel != null)      meta.setItemModel(itemModel);
            if (tooltipStyle != null)   meta.setTooltipStyle(tooltipStyle);

            meta.setUnbreakable(unbreakable);

            if (enchantability != null) {
                meta.setEnchantable(enchantability.value());
            }

            if (!enchantments.isEmpty()) {
                if (meta instanceof EnchantmentStorageMeta enchantedBookMeta) {
                    this.enchantments.forEach((enchantment, integer) -> enchantedBookMeta.addStoredEnchant(enchantment, integer, true));
                } else {
                    this.enchantments.forEach((enchantment, integer) -> meta.addEnchant(enchantment, integer, true));
                }
            }

            meta.setEnchantmentGlintOverride(showEnchantGlint);

            meta.setHideTooltip(hideTooltip);
            meta.setRarity(itemRarity);
            meta.setMaxStackSize(maxStackSize);
            meta.setGlider(glider);

            if (!attributeModifiers.isEmpty()) attributeModifiers.forEach(meta::addAttributeModifier);
            if (!itemFlags.isEmpty()) this.itemFlags.forEach(meta::addItemFlags);

            itemStack.setItemMeta(meta);

            if (armorTrim != null) {
                if (!(meta instanceof ArmorMeta armorMeta)) {
                    throw new IllegalArgumentException("Cannot set armor trim on an item that is not armor!");
                }

                armorMeta.setTrim(armorTrim);
                itemStack.setItemMeta(armorMeta);
            }
        }

        itemStack.setAmount(amount);

        if (customModelData != null)    itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData.build());
        if (instrument != null)         itemStack.setData(DataComponentTypes.INSTRUMENT, instrument);
        if (damageTaken != null)        itemStack.setData(DataComponentTypes.DAMAGE, damageTaken);
        if (maxDamage != null)          itemStack.setData(DataComponentTypes.MAX_DAMAGE, maxDamage);
        if (repairCost != null)         itemStack.setData(DataComponentTypes.REPAIR_COST, repairCost);
        if (consumable != null)         itemStack.setData(DataComponentTypes.CONSUMABLE, consumable);
        if (foodProperties != null)     itemStack.setData(DataComponentTypes.FOOD, foodProperties);
        if (equippable != null)         itemStack.setData(DataComponentTypes.EQUIPPABLE, equippable);
        if (deathProtection != null)    itemStack.setData(DataComponentTypes.DEATH_PROTECTION, deathProtection);
        if (tool != null)               itemStack.setData(DataComponentTypes.TOOL, tool.build());
        if (damageResistant != null)    itemStack.setData(DataComponentTypes.DAMAGE_RESISTANT,
                DamageResistant.damageResistant(TagKey.create(RegistryKey.DAMAGE_TYPE, damageResistant.key())));

        if (bannerPatterns != null && (itemStack.getType().name().endsWith("BANNER") || itemStack.getType() == Material.SHIELD)) {
            itemStack.setData(DataComponentTypes.BANNER_PATTERNS, bannerPatterns.build());
        }

        if (color != null) {
            if (this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_CHESTPLATE
                    || this.material == Material.LEATHER_LEGGINGS || this.material == Material.LEATHER_BOOTS) {
                itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color));
            } else if (this.material == Material.SHIELD) {
                itemStack.setData(DataComponentTypes.BASE_COLOR, Objects.requireNonNull(DyeColor.getByColor(color)));
            } else if (this.material.name().endsWith("POTION") && potionContents != null) {
                potionContents.customColor(color);
            } else {
                throw new IllegalArgumentException("Cannot set color on an item that is not armor, a shield, or a potion!");
            }
        }

        if (potionContents != null && (itemStack.getType().name().endsWith("POTION") || itemStack.getType() == Material.TIPPED_ARROW)) {
            itemStack.setData(DataComponentTypes.POTION_CONTENTS, potionContents.build());
        }

        if (material == Material.PLAYER_HEAD) {
            if (profile != null) {
                itemStack.setData(DataComponentTypes.PROFILE, profile.build());
            } else if (playerProfile != null) {
                itemStack.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(playerProfile));
            }
        }

        return itemStack;
    }

}
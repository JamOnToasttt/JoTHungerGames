package me.jamontoast.jothungergames.Utilities;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SponsorShop {

    public static class ShopItem {
        private final String name;
        private final ItemStack item;
        private final int cost;
        private final String category;

        public ShopItem(String name, ItemStack item, int cost, String category) {
            this.name = name;
            this.item = item;
            this.cost = cost;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public ItemStack getItem() {
            return item.clone();
        }

        public int getCost() {
            return cost;
        }

        public String getCategory() {
            return category;
        }

        public ItemStack getDisplayItem() {
            ItemStack display = item.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + name);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Cost: " + SpectatorCurrency.formatCurrencyDisplay(cost));
                lore.add(ChatColor.GREEN + "Select to send gift to tribute");
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            return display;
        }
    }

    private static final List<ShopItem> SHOP_ITEMS = new ArrayList<>();

    static {
        initializeShop();
    }

    private static void initializeShop() {
        SHOP_ITEMS.add(new ShopItem("Enchanted Golden Apple", new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1), 300, "Food"));
        SHOP_ITEMS.add(new ShopItem("Golden Apple", new ItemStack(Material.GOLDEN_APPLE, 1), 100, "Food"));
        SHOP_ITEMS.add(new ShopItem("Golden Carrot", new ItemStack(Material.GOLDEN_CARROT, 1), 30, "Food"));
        SHOP_ITEMS.add(new ShopItem("Cooked Beef x4", new ItemStack(Material.COOKED_BEEF, 4), 10, "Food"));
        SHOP_ITEMS.add(new ShopItem("Bread x4", new ItemStack(Material.BREAD, 4), 5, "Food"));

        // POTIONS CATEGORY
        SHOP_ITEMS.add(createPotionItem("Splash Potion of Strength", PotionEffectType.STRENGTH, 1, 45, 225, "Potions"));
        SHOP_ITEMS.add(createPotionItem("Potion of Strength", PotionEffectType.STRENGTH, 1, 60, 200, "Potions"));

        SHOP_ITEMS.add(createPotionItem("Strong Splash Potion of Regeneration", PotionEffectType.REGENERATION, 2, 22, 200, "Potions"));
        SHOP_ITEMS.add(createPotionItem("Splash Potion of Regeneration", PotionEffectType.REGENERATION, 1, 45, 175, "Potions"));
        SHOP_ITEMS.add(createPotionItem("Potion of Regeneration", PotionEffectType.REGENERATION, 1, 45, 150, "Potions"));

        SHOP_ITEMS.add(createPotionItem("Potion of Speed", PotionEffectType.SPEED, 1, 60, 125, "Potions"));

        SHOP_ITEMS.add(createPotionItem("Potion of Health", PotionEffectType.INSTANT_HEALTH, 2, 0, 100, "Potions"));

        SHOP_ITEMS.add(createPotionItem("Potion of Fire Resistance", PotionEffectType.FIRE_RESISTANCE, 1, 480, 75, "Potions"));



        // WEAPONS CATEGORY
        ItemStack reaperScythe = new ItemStack(Material.NETHERITE_HOE);
        reaperScythe.addUnsafeEnchantment(Enchantment.SHARPNESS, 20);
        reaperScythe.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 3);
        SHOP_ITEMS.add(new ShopItem("Fine-edged Scythe", reaperScythe, 350, "Weapons"));

        ItemStack sharpnessAxe = new ItemStack(Material.IRON_AXE);
        sharpnessAxe.addEnchantment(Enchantment.SHARPNESS, 2);
        SHOP_ITEMS.add(new ShopItem("Hefty Iron Axe", sharpnessAxe, 225, "Weapons"));
        ItemStack sharpnessSword = new ItemStack(Material.IRON_SWORD);
        sharpnessAxe.addEnchantment(Enchantment.SHARPNESS, 2);
        SHOP_ITEMS.add(new ShopItem("Honed Iron Sword", sharpnessSword, 175, "Weapons"));
        ItemStack lungeSpear = new ItemStack(Material.IRON_SPEAR);
        lungeSpear.addEnchantment(Enchantment.SHARPNESS, 1);
        lungeSpear.addEnchantment(Enchantment.LUNGE, 2);
        SHOP_ITEMS.add(new ShopItem("Reinforced Iron Spear", lungeSpear, 75, "Weapons"));

        ItemStack ironAxe = new ItemStack(Material.IRON_AXE);
        SHOP_ITEMS.add(new ShopItem("Polished Iron Axe", ironAxe, 150, "Weapons"));
        ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
        SHOP_ITEMS.add(new ShopItem("Polished Iron Sword", ironSword, 65, "Weapons"));
        ItemStack ironSpear = new ItemStack(Material.IRON_SPEAR);
        SHOP_ITEMS.add(new ShopItem("Polished Iron Spear", ironSpear, 75, "Weapons"));


        ItemStack longbow = new ItemStack(Material.BOW);
        longbow.addEnchantment(Enchantment.PUNCH, 2);
        longbow.addEnchantment(Enchantment.POWER, 2);
        SHOP_ITEMS.add(new ShopItem("Longbow", longbow, 145, "Weapons"));
        ItemStack shortbow = new ItemStack(Material.BOW);
        shortbow.addEnchantment(Enchantment.PUNCH, 2);
        SHOP_ITEMS.add(new ShopItem("Shortbow", shortbow, 115, "Weapons"));
        SHOP_ITEMS.add(new ShopItem("Arrows x16", new ItemStack(Material.ARROW, 16), 30, "Weapons"));

        // ARMOR CATEGORY
        ItemStack diamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        SHOP_ITEMS.add(new ShopItem("Pristine Diamond Chestplate", diamondChestplate, 325, "Armor"));

        ItemStack spikedIronHelmet = new ItemStack(Material.IRON_HELMET);
        spikedIronHelmet.addEnchantment(Enchantment.THORNS, 1);
        SHOP_ITEMS.add(new ShopItem("Spiked Iron Helmet", spikedIronHelmet, 125, "Armor"));
        ItemStack spikedIronChestplate = new ItemStack(Material.IRON_CHESTPLATE);
        spikedIronChestplate.addEnchantment(Enchantment.THORNS, 1);
        SHOP_ITEMS.add(new ShopItem("Spiked Iron Chestplate",spikedIronChestplate, 200, "Armor"));
        ItemStack spikedIronLeggings = new ItemStack(Material.IRON_LEGGINGS);
        spikedIronLeggings.addEnchantment(Enchantment.THORNS, 1);
        SHOP_ITEMS.add(new ShopItem("Spiked Iron Leggings",spikedIronLeggings, 175, "Armor"));
        ItemStack spikedIronBoots = new ItemStack(Material.IRON_BOOTS);
        spikedIronBoots.addEnchantment(Enchantment.THORNS, 1);
        SHOP_ITEMS.add(new ShopItem("Spiked Iron Boots", spikedIronBoots, 125, "Armor"));




    }


    private static ShopItem createPotionItem(String name, PotionEffectType effect, int amplifier, int duration, int cost, String category) {
        ItemStack potion = new ItemStack(Material.POTION);
        if (name.contains("Splash")) {
            potion = new ItemStack(Material.SPLASH_POTION);
        } else {
            potion = new ItemStack(Material.POTION);

        }
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta != null) {
            meta.addCustomEffect(new PotionEffect(effect, duration * 20, amplifier - 1), true);
            potion.setItemMeta(meta);
        }
        return new ShopItem(name, potion, cost, category);
    }

    public static List<ShopItem> getAllItems() {
        return new ArrayList<>(SHOP_ITEMS);
    }

    public static List<ShopItem> getItemsByCategory(String category) {
        List<ShopItem> items = new ArrayList<>();
        for (ShopItem item : SHOP_ITEMS) {
            if (item.getCategory().equals(category)) {
                items.add(item);
            }
        }
        return items;
    }

    public static List<String> getCategories() {
        return Arrays.asList("Food", "Potions", "Weapons", "Armor");
    }
}

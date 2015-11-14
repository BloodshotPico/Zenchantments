package zedly.zenchantments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import static org.bukkit.Material.ARROW;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Config {

    private HashMap<String, CustomEnchantment> enchants = new HashMap<>();
    private HashMap<String, CustomArrow> arrows = new HashMap<>();
    private final double enchantRarity;
    private final int maxEnchants;
    private final boolean enchantPVP;
    private final int shredDrops;
    private final boolean explosionBlockBreak;
    private final boolean descriptionLore;
    private ChatColor descriptionColor = ChatColor.GREEN;
    private final World world;

    public Config(HashMap<String, CustomEnchantment> enchants, HashMap<String, CustomArrow> arrows, double enchantRarity,
            int maxEnchants, boolean enchantPVP, int shredDrops, boolean explosionBlockBreak,
            boolean descriptionLore, ChatColor descriptionColor, World world) {
        this.enchants = enchants;
        this.arrows = arrows;
        this.enchantRarity = enchantRarity;
        this.maxEnchants = maxEnchants;
        this.enchantPVP = enchantPVP;
        this.shredDrops = shredDrops;
        this.explosionBlockBreak = explosionBlockBreak;
        this.descriptionLore = descriptionLore;
        this.descriptionColor = descriptionColor;
        this.world = world;
    }

    public HashMap<String, CustomEnchantment> getEnchants() {
        return enchants;
    }

    public HashMap<String, CustomArrow> getArrows() {
        return arrows;
    }

    public double getEnchantRarity() {
        return enchantRarity;
    }

    public int getMaxEnchants() {
        return maxEnchants;
    }

    public boolean enchantPVP() {
        return enchantPVP;
    }

    public int getShredDrops() {
        return shredDrops;
    }

    public boolean explosionBlockBreak() {
        return explosionBlockBreak;
    }

    public boolean descriptionLore() {
        return descriptionLore;
    }

    public ChatColor getDescriptionColor() {
        return descriptionColor;
    }

    public World getWorld() {
        return world;
    }

    public static void loadConfigs() {
        Storage.worldConfigs.clear();
        for (World world : Bukkit.getWorlds()) {
            try {
                //Make the file if needed
                InputStream stream = Zenchantments.class.getResourceAsStream("/resource/defaultconfig.yml");
                File file = new File("plugins/Zenchantments/" + world.getName() + ".yml");
                if (!file.exists()) {
                    try {
                        String raw = IOUtils.toString(stream, "UTF-8");
                        byte[] b = raw.getBytes();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(b, 0, b.length);
                        fos.flush();
                    } catch (IOException e) {
                    }
                }
                YamlConfiguration c = new YamlConfiguration();
                c.load(file);
                //Init variables
                HashMap<String, CustomEnchantment> enchants = new HashMap<>();
                HashMap<String, CustomArrow> arrows = new HashMap<>();
                final double enchantRarity;
                final int maxEnchants;
                final boolean enchantPVP;
                final int shredDrops;
                final boolean explosionBlockBreak;
                final boolean descriptionLore;
                ChatColor descriptionColor = ChatColor.GREEN;
                //Elemental Arrows
                ArrayList<Class> toRemove = new ArrayList<>();
                for (int x = c.getList("elemental_arrows").size() - 1;
                        x >= 0; x--) {
                    String str = "" + c.getList("elemental_arrows").get(x);
                    boolean b;
                    try {
                        b = Boolean.parseBoolean(str.split("=")[1].replace("}", ""));
                    } catch (NumberFormatException e) {
                        b = false;
                    }
                    for (Class cl : CustomArrow.class.getClasses()) {
                        try {
                            CustomArrow ar = (CustomArrow) cl.newInstance();
                            if (ar.getName() != null) {
                                if (ar.getName().equals(str.split("=")[0].replace("{", "")) && !b) {
                                    toRemove.add(cl);
                                }
                            }
                        } catch (InstantiationException | IllegalAccessException ex) {
                        }
                    }
                }
                //Load Arrows & Recipes
                ItemStack is = new ItemStack(ARROW);
                ItemMeta meta = is.getItemMeta();
                for (Class cl : CustomArrow.class.getClasses()) {
                    try {
                        if (!toRemove.contains(cl)) {
                            CustomArrow ar = (CustomArrow) cl.newInstance();
                            if (ar.getRecipe(is) != null) {
                                ArrayList<String> lore = new ArrayList<>();
                                lore.add(ChatColor.AQUA + ar.getName());
                                meta.setLore(lore);
                                is.setItemMeta(meta);
                                Bukkit.getServer().addRecipe(ar.getRecipe(is));
                            }
                            if (ar.getName() != null) {
                                arrows.put(ar.getName(), ar);
                            }
                        }
                    } catch (InstantiationException | IllegalAccessException ex) {
                    }
                }
                //Load Variables
                int rarity = (int) c.get("enchant_rarity");
                enchantRarity = ((double) rarity / 100.0);
                maxEnchants = (int) c.get("max_enchants");
                enchantPVP = (boolean) c.get("enchant_PVP");
                explosionBlockBreak = (boolean) c.get("explosion_block_break");
                descriptionLore = (boolean) c.get("description_lore");
                ChatColor color = ChatColor.getByChar("" + c.get("description_color"));
                if (color != null) {
                    descriptionColor = color;
                }
                switch ((String) c.get("shred_drops")) {
                    case "all":
                        shredDrops = 0;
                        break;
                    case "block":
                        shredDrops = 1;
                        break;
                    case "none":
                        shredDrops = 2;
                        break;
                    default:
                        shredDrops = 0;
                }
                //Load Individual CustomEnchantment Configs
                HashMap<String, ArrayList<String>> tempConfigs = new HashMap<>();
                for (int x = 0; x < c.getList("enchantments").size(); x++) {
                    String rawConfig = ("" + c.getList("enchantments").get(x)).replace("}", "").replace("{", "");
                    String[] p = rawConfig.replace(", ", ",").split("=");
                    ArrayList<String> parts = new ArrayList<>();
                    parts.add(p[2].split(",")[0]);
                    parts.add(p[4].split(",")[0]);
                    parts.add(p[5].split(",")[0]);
                    for (int i = 0; i < p[3].split(",").length - 1; i++) {
                        parts.add(p[3].split(",")[i]);
                    }
                    tempConfigs.put(rawConfig.subSequence(0, rawConfig.indexOf("=")).toString().replace(" ", "").toLowerCase(), parts);
                }
                //Load CustomEnchantment Classes
                for (Class cl : CustomEnchantment.class.getClasses()) {
                    try {
                        CustomEnchantment ench = (CustomEnchantment) cl.newInstance();
                        if (tempConfigs.containsKey(ench.loreName.toLowerCase().replace(" ", ""))) {
                            ArrayList<String> conf = tempConfigs.get(ench.loreName.toLowerCase().replace(" ", ""));
                            float probability = 1;
                            try {
                                probability = Float.parseFloat(conf.get(0));
                            } catch (NumberFormatException e) {
                            }
                            ench.chance = probability;
                            ench.loreName = conf.get(1);
                            int max = 1;
                            try {
                                max = Integer.parseInt(conf.get(2));
                            } catch (NumberFormatException e) {
                            }
                            ench.maxLevel = max;
                            Object[] m = null;
                            for (int i = 3; i < conf.size(); i++) {
                                switch (conf.get(i)) {
                                    case "Axe":
                                        m = ArrayUtils.addAll(m, Storage.axes);
                                        break;
                                    case "Shovel":
                                        m = ArrayUtils.addAll(m, Storage.spades);
                                        break;
                                    case "Sword":
                                        m = ArrayUtils.addAll(m, Storage.swords);
                                        break;
                                    case "Pickaxe":
                                        m = ArrayUtils.addAll(m, Storage.picks);
                                        break;
                                    case "Rod":
                                        m = ArrayUtils.addAll(m, Storage.rods);
                                        break;
                                    case "Shears":
                                        m = ArrayUtils.addAll(m, Storage.shears);
                                        break;
                                    case "Bow":
                                        m = ArrayUtils.addAll(m, Storage.bows);
                                        break;
                                    case "Lighter":
                                        m = ArrayUtils.addAll(m, Storage.lighters);
                                        break;
                                    case "Hoe":
                                        m = ArrayUtils.addAll(m, Storage.hoes);
                                        break;
                                    case "Helmet":
                                        m = ArrayUtils.addAll(m, Storage.helmets);
                                        break;
                                    case "Chestplate":
                                        m = ArrayUtils.addAll(m, Storage.chestplates);
                                        break;
                                    case "Leggings":
                                        m = ArrayUtils.addAll(m, Storage.leggings);
                                        break;
                                    case "Boots":
                                        m = ArrayUtils.addAll(m, Storage.boots);
                                        break;
                                    case "All":
                                        m = ArrayUtils.addAll(m, Storage.axes);
                                        m = ArrayUtils.addAll(m, Storage.spades);
                                        m = ArrayUtils.addAll(m, Storage.swords);
                                        m = ArrayUtils.addAll(m, Storage.picks);
                                        m = ArrayUtils.addAll(m, Storage.rods);
                                        m = ArrayUtils.addAll(m, Storage.shears);
                                        m = ArrayUtils.addAll(m, Storage.bows);
                                        m = ArrayUtils.addAll(m, Storage.lighters);
                                        m = ArrayUtils.addAll(m, Storage.hoes);
                                        m = ArrayUtils.addAll(m, Storage.helmets);
                                        m = ArrayUtils.addAll(m, Storage.chestplates);
                                        m = ArrayUtils.addAll(m, Storage.leggings);
                                        m = ArrayUtils.addAll(m, Storage.boots);
                                        break;
                                }
                            }
                            ench.enchantable = (Material[]) m;
                        }
                        if (ench.chance != -1) {
                            enchants.put(ench.loreName.toLowerCase().replace(" ", ""), ench);
                        }
                    } catch (InstantiationException | IllegalAccessException ex) {
                    }
                }
                Storage.worldConfigs.add(new Config(enchants, arrows, enchantRarity, maxEnchants, enchantPVP, shredDrops, explosionBlockBreak, descriptionLore, descriptionColor, world));
            } catch (IOException | InvalidConfigurationException ex) {
            }
        }
    }

    public static Config get(World world) {
        for (Config c : Storage.worldConfigs) {
            if (c.world.equals(world)) {
                return c;
            }
        }
        loadConfigs();
        for (Config c : Storage.worldConfigs) {
            if (c.world.equals(world)) {
                return c;
            }
        }
        return null;
    }

    public LinkedHashMap<CustomEnchantment, Integer> getEnchants(ItemStack stk) {
        ItemStack stack;
        LinkedHashMap<CustomEnchantment, Integer> map = new LinkedHashMap<>();
        if (stk != null) {
            stack = removeDescriptions(stk.clone(), null);
            if (stack.hasItemMeta()) {
                if (stack.getItemMeta().hasLore()) {
                    List<String> lore = stack.getItemMeta().getLore();
                    for (String rawEnchant : lore) {
                        int index1 = rawEnchant.lastIndexOf(" ");
                        if (index1 == -1) {
                            continue;
                        }
                        Integer level = Utilities.getNumber(rawEnchant.substring(index1 + 1));
                        String enchant = rawEnchant.substring(2, index1);
                        if (getEnchants().containsKey(enchant.replace(" ", "").toLowerCase())) {
                            CustomEnchantment ench = getEnchants().get(enchant.replace(" ", "").toLowerCase());
                            map.put(ench, level);
                        }
                    }
                }
            }
        }
        LinkedHashMap<CustomEnchantment, Integer> finalmap = new LinkedHashMap<>();
        for (Class c : new Class[]{CustomEnchantment.Lumber.class, CustomEnchantment.Shred.class, CustomEnchantment.Mow.class, CustomEnchantment.Extraction.class}) {
            CustomEnchantment e = null;
            for (CustomEnchantment en : getEnchants().values()) {
                if (en.getClass().equals(c)) {
                    e = en;
                }
            }
            if (map.containsKey(e)) {
                finalmap.put(e, map.get(e));
                map.remove(e);
            }
        }
        finalmap.putAll(map);
        return finalmap;
    }

    private CustomEnchantment getEnchant(String raw) {
        CustomEnchantment e = null;
        int index1 = raw.lastIndexOf(" ");
        if (index1 == -1) {
            return e;
        }
        String enchant = raw.substring(2, index1);
        if (getEnchants().containsKey(enchant.replace(" ", "").toLowerCase())) {
            e = getEnchants().get(enchant.replace(" ", "").toLowerCase());
        }
        return e;
    }

    public ItemStack addDescriptions(ItemStack stk, CustomEnchantment delete) {
        stk = removeDescriptions(stk, delete);
        if (stk != null) {
            if (stk.hasItemMeta()) {
                if (stk.getItemMeta().hasLore()) {
                    ItemMeta meta = stk.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    for (String s : meta.getLore()) {
                        lore.add(s);
                        CustomEnchantment e = getEnchant(s);
                        if (e != null) {
                            String str = e.description;
                            int start = 0;
                            int counter = 0;
                            for (int i = 0; i < str.toCharArray().length; i++) {
                                if (counter > 30) {
                                    if (str.toCharArray()[i - 1] == ' ') {
                                        lore.add(getDescriptionColor() + str.substring(start, i));
                                        counter = 0;
                                        start = i;
                                    }
                                }
                                counter++;
                            }
                            lore.add(getDescriptionColor() + str.substring(start));
                        }
                    }
                    meta.setLore(lore);
                    stk.setItemMeta(meta);
                }
            }
        }
        return stk;
    }

    public ItemStack removeDescriptions(ItemStack stk, CustomEnchantment delete) {
        if (stk != null) {
            if (stk.hasItemMeta()) {
                if (stk.getItemMeta().hasLore()) {
                    ItemMeta meta = stk.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    CustomEnchantment current = null;
                    for (String s : meta.getLore()) {
                        CustomEnchantment e = getEnchant(s);
                        if (e != null) {
                            current = e;
                        }
                        if (current == null) {
                            if (delete != null) {
                                if (!delete.description.contains(ChatColor.stripColor(s))) {
                                    lore.add(s);
                                }
                            } else {
                                lore.add(s);
                            }
                        } else if (delete != null) {
                            if (!delete.description.contains(ChatColor.stripColor(s)) && !current.description.contains(ChatColor.stripColor(s))) {
                                lore.add(s);
                            }
                        } else if (!current.description.contains(ChatColor.stripColor(s))) {
                            lore.add(s);
                        }
                    }
                    meta.setLore(lore);
                    stk.setItemMeta(meta);
                    return stk;
                }
            }
        }
        return stk;
    }
}

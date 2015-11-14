package zedly.zenchantments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import static org.bukkit.Material.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandProcessor {

    private static ItemStack theLore(Player player, CustomEnchantment enchantment, ItemStack stack, String level, boolean isHeld) {
        Config config = Config.get(player.getWorld());
        if (stack.getType() == AIR) {
            player.sendMessage(Storage.logo + "You need to be holding an item!");
            return stack;
        }
        if (!(ArrayUtils.contains(enchantment.enchantable, stack.getType())) && stack.getType() != BOOK && stack.getType() != ENCHANTED_BOOK) {
            player.sendMessage(Storage.logo + "The enchantment " + ChatColor.DARK_AQUA + enchantment.loreName + ChatColor.AQUA + " cannot be added to this item.");
            return stack;
        }
        try {
            level = Utilities.getRomanString(Integer.parseInt(level), enchantment.maxLevel);
        } catch (NumberFormatException e) {
            level = "I";
        }
        if (stack.getType() == BOOK) {
            stack.setType(ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
            meta.addStoredEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            stack.setItemMeta(meta);
        }
        String finalEnch = ChatColor.GRAY + enchantment.loreName + " " + level;
        List<String> lore = new ArrayList<>();
        if (stack.getItemMeta().hasLore()) {
            lore = stack.getItemMeta().getLore();
        }
        if (config.getEnchants(stack).containsKey(enchantment)) {
            Iterator it = lore.iterator();
            while (it.hasNext()) {
                String rawEnchant = (String) it.next();
                if (rawEnchant.contains(enchantment.loreName)) {
                    it.remove();
                }
            }
        }
        if (!level.equals("-")) {
            lore.add(finalEnch);
            if (isHeld) {
                player.sendMessage(Storage.logo + "The enchantment " + ChatColor.DARK_AQUA + enchantment.loreName + ChatColor.AQUA + " has been added.");
            }
        } else {
            if (!isHeld) {
                return null;
            }
            player.sendMessage(Storage.logo + "The enchantment " + ChatColor.DARK_AQUA + enchantment.loreName + ChatColor.AQUA + " has been removed.");
        }
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return config.descriptionLore() ? config.addDescriptions(stack, enchantment) : stack;
    }

    public static boolean onCommand(CommandSender sender, Command command, String commandlabel, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        Config config = Config.get(player.getWorld());
        ItemStack stack = player.getItemInHand();
        String lArgs = "";
        if (!(args.length == 0)) {
            lArgs = args[0].toLowerCase().replace("_", "");
        }
        String cmd = commandlabel.toLowerCase();
        switch (cmd) {
            case "ench":
                switch (lArgs) {
                    case "reload":
                        if (!sender.hasPermission("zenchantments.command.reload")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        player.sendMessage(Storage.logo + "Reloaded Zenchantments.");
                        Storage.zenchantments.loadConfigs();
                        break;
                    case "give":
                        if (!sender.hasPermission("zenchantments.command.give")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        if (args.length == 5) {
                            CustomEnchantment ench;
                            if (config.getEnchants().containsKey(args[2].toLowerCase().replace("_", ""))) {
                                ench = config.getEnchants().get(args[2].replace("_", "").toLowerCase());
                            } else {
                                player.sendMessage(Storage.logo + "That enchantment does not exist!");
                                return true;
                            }
                            Material mat = Material.matchMaterial(args[4].toUpperCase());
                            if (mat == null) {
                                player.sendMessage(Storage.logo + "The material " + ChatColor.DARK_AQUA + args[4].toUpperCase() + ChatColor.AQUA + " is not valid.");
                                return true;
                            }
                            if (!ArrayUtils.contains(ench.enchantable, mat) && !mat.equals(BOOK)) {
                                player.sendMessage(Storage.logo + "The enchantment " + ChatColor.DARK_AQUA + ench.loreName + ChatColor.AQUA + " cannot be added to this item.");
                                return true;
                            }
                            ItemStack stk = theLore(player, ench, new ItemStack(mat), args[3], false);
                            Player toAdd = null;
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getName().equalsIgnoreCase(args[1])) {
                                    toAdd = p;
                                }
                            }
                            if (toAdd != null) {
                                if (stk != null) {
                                    toAdd.getInventory().addItem(stk);
                                    player.sendMessage(Storage.logo + "Gave " + ChatColor.DARK_AQUA + toAdd.getName() + ChatColor.AQUA + " the enchantment " + ChatColor.DARK_AQUA + ench.loreName + ChatColor.AQUA + ".");
                                } else {
                                    player.sendMessage(Storage.logo + "You can't give a level 0 enchantment!");
                                }
                            } else {
                                player.sendMessage(Storage.logo + "Player " + ChatColor.DARK_AQUA + args[1] + ChatColor.AQUA + " is not online.");
                            }
                        } else {
                            player.sendMessage(Storage.logo + "<Player> <enchantment> <?level> <Material>");
                        }
                        break;
                    case "list":
                        if (!sender.hasPermission("zenchantments.command.list")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        player.sendMessage(Storage.logo + "Enchantment Types:");
                        for (String str : config.getEnchants().keySet()) {
                            if (ArrayUtils.contains(config.getEnchants().get(str.replace(" ", "").toLowerCase()).enchantable, stack.getType())) {
                                player.sendMessage(ChatColor.DARK_AQUA + "- " + ChatColor.AQUA + config.getEnchants().get(str.replace(" ", "").toLowerCase()).loreName);
                            }
                        }
                        break;
                    case "info":
                        if (!sender.hasPermission("zenchantments.command.info")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        if (args.length > 1) {
                            String enchant = WordUtils.capitalize(args[1].toLowerCase().replace("_", " "));
                            if (config.getEnchants().containsKey(enchant.replace(" ", "").toLowerCase())) {
                                CustomEnchantment ench = config.getEnchants().get(enchant.replace(" ", "").toLowerCase());
                                String e = "";
                                if (Storage.playerSettings.containsKey(player.getUniqueId())) {
                                    if (Storage.playerSettings.get(player.getUniqueId()).contains(ench)) {
                                        e = ChatColor.RED + "**Disabled** ";
                                    }
                                }
                                player.sendMessage(Storage.logo + (ench.loreName + ": " + e + ChatColor.AQUA + ench.description).replace(ChatColor.GRAY + "", ""));
                            }
                        } else {
                            player.sendMessage(Storage.logo + "Enchantment Info:");
                            for (CustomEnchantment e : config.getEnchants(player.getItemInHand()).keySet()) {
                                String s = "";
                                if (Storage.playerSettings.containsKey(player.getUniqueId())) {
                                    if (Storage.playerSettings.get(player.getUniqueId()).contains(e)) {
                                        s = ChatColor.RED + "**Disabled** ";
                                    }
                                }
                                player.sendMessage((ChatColor.DARK_AQUA + e.loreName + ": " + s + ChatColor.AQUA + e.description).replace(ChatColor.GRAY + "", ""));
                            }
                        }
                        break;
                    case "disable":
                        if (!sender.hasPermission("zenchantments.command.onoff")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        if (args.length > 1) {
                            String enchant = WordUtils.capitalize(args[1].toLowerCase().replace("_", " "));
                            if (config.getEnchants().containsKey(enchant.replace(" ", "").toLowerCase())) {
                                CustomEnchantment ench = config.getEnchants().get(enchant.replace(" ", "").toLowerCase());
                                HashSet<CustomEnchantment> enchs = new HashSet<>();
                                if (Storage.playerSettings.containsKey(player.getUniqueId())) {
                                    enchs = Storage.playerSettings.get(player.getUniqueId());
                                }
                                enchs.add(ench);
                                Storage.playerSettings.put(player.getUniqueId(), enchs);
                                PlayerConfig.saveConfigs();
                                player.sendMessage(Storage.logo + "The enchantment " + ChatColor.DARK_AQUA + ench.loreName + ChatColor.AQUA + " has been disabled.");
                            } else if (args[1].toLowerCase().equals("all")) {
                                HashSet<CustomEnchantment> enchs = new HashSet<>();
                                for (CustomEnchantment e : config.getEnchants().values()) {
                                    enchs.add(e);
                                }
                                Storage.playerSettings.put(player.getUniqueId(), enchs);
                                PlayerConfig.saveConfigs();
                                player.sendMessage(Storage.logo + ChatColor.DARK_AQUA + "All " + ChatColor.AQUA + "enchantments have been disabled.");
                            } else {
                                player.sendMessage(Storage.logo + "That enchantment does not exist!");
                            }
                        }
                        break;
                    case "enable":
                        if (!sender.hasPermission("zenchantments.command.onoff")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        if (args.length > 1) {
                            String enchant = WordUtils.capitalize(args[1].toLowerCase().replace("_", " "));
                            if (config.getEnchants().containsKey(enchant.replace(" ", "").toLowerCase())) {
                                CustomEnchantment ench = config.getEnchants().get(enchant.replace(" ", "").toLowerCase());
                                if (Storage.playerSettings.containsKey(player.getUniqueId())) {
                                    Storage.playerSettings.get(player.getUniqueId()).remove(ench);
                                    if (Storage.playerSettings.get(player.getUniqueId()).isEmpty()) {
                                        Storage.playerSettings.remove(player.getUniqueId());
                                    }
                                    PlayerConfig.saveConfigs();
                                }
                                player.sendMessage(Storage.logo + "The enchantment " + ChatColor.DARK_AQUA + ench.loreName + ChatColor.AQUA + " has been enabled.");
                            } else if (args[1].toLowerCase().equals("all")) {
                                Storage.playerSettings.remove(player.getUniqueId());
                                PlayerConfig.saveConfigs();
                                player.sendMessage(Storage.logo + ChatColor.DARK_AQUA + "All " + ChatColor.AQUA + "enchantments have been enabled.");
                            } else {
                                player.sendMessage(Storage.logo + "That enchantment does not exist!");
                            }
                        }
                        break;
                    case "help":
                    default:
                        if (lArgs.equals("") || lArgs.equals("help")) {
                            player.sendMessage(Storage.logo);
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "ench info <?enchantment>: " + ChatColor.AQUA + "Returns information about custom enchantments.");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "ench list: " + ChatColor.AQUA + "Returns a list of enchantments for the tool in hand.");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "ench give <Player> <enchantment> <?level> <Material> " + ChatColor.AQUA + "Gives the target a specified enchanted item.");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "ench <enchantment> <?level>: " + ChatColor.AQUA + "Enchants the item in hand with the given enchantment and level");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "ench disable <enchantment/all>: " + ChatColor.AQUA + "Disables selected enchantment for the user");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "ench enable <enchantment/all>: " + ChatColor.AQUA + "Enables selected enchantment for the user");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "ench enable <enchantment/all>: " + ChatColor.AQUA + "Enables selected enchantment for the user");
                            break;
                        }
                        if (!sender.hasPermission("zenchantments.command.enchant")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        if (config.getEnchants().containsKey(lArgs.toLowerCase().replace("_", ""))) {
                            CustomEnchantment ench = config.getEnchants().get(lArgs.toLowerCase().replace("_", "").toLowerCase());
                            if (args.length >= 2) {
                                player.setItemInHand(theLore(player, ench, stack, args[1], true));
                            } else {
                                player.setItemInHand(theLore(player, ench, stack, "1", true));
                            }
                        } else {
                            player.sendMessage(Storage.logo + "That enchantment does not exist!");
                        }
                }
                break;
            case "arrow":
                switch (lArgs) {
                    case "list":
                        if (!sender.hasPermission("zenchantments.command.list")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        player.sendMessage(Storage.logo + "Arrow Types:");
                        for (String str : config.getArrows().keySet()) {
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + ChatColor.AQUA + str);
                        }
                        break;
                    case "info":
                        if (!sender.hasPermission("zenchantments.command.info")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        if (args.length >= 2) {
                            for (CustomArrow ar : config.getArrows().values()) {
                                if (ar.getName().toLowerCase().startsWith(args[1])) {
                                    player.sendMessage(Storage.logo + "Arrow Info:");
                                    player.sendMessage(ChatColor.DARK_AQUA + "- " + ar.getName() + ": " + ChatColor.AQUA + ar.getDescription());
                                    return true;
                                }
                            }
                            player.sendMessage(Storage.logo + "Could not find the type of Arrow you're looking for!");
                        } else {
                            if (stack.getType() == ARROW) {
                                if (stack.getItemMeta().hasLore()) {
                                    String str = stack.getItemMeta().getLore().get(0);
                                    str = ChatColor.stripColor(str);
                                    for (String string : config.getArrows().keySet()) {
                                        if (string.equals(str)) {
                                            player.sendMessage(Storage.logo + "Arrow Info:");
                                            player.sendMessage(ChatColor.DARK_AQUA + "- " + str + ": " + ChatColor.AQUA + config.getArrows().get(str).getDescription());
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case "help":
                    default:
                        if (lArgs.equals("") || lArgs.equals("help")) {
                            player.sendMessage(ChatColor.BLUE + "[" + ChatColor.DARK_AQUA + "Zenchantments" + ChatColor.BLUE + "] ");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "arrow info: " + ChatColor.AQUA + "Returns information about custom arrows.");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "arrow list: " + ChatColor.AQUA + "Returns a list of custom arrows");
                            player.sendMessage(ChatColor.DARK_AQUA + "- " + "arrow <arrow type> <?arguments> <?arguments>: " + ChatColor.AQUA + "Adds the desired arrow effect to the arrow in hand.");
                            break;
                        }
                        if (!sender.hasPermission("zenchantments.command.arrow")) {
                            player.sendMessage(Storage.logo + "You do not have permission to do this!");
                            return true;
                        }
                        if (player.getItemInHand() == null || player.getItemInHand().getType() != ARROW) {
                            player.sendMessage(Storage.logo + "You need to be holding arrows for this command!");
                            return true;
                        }
                        for (CustomArrow ar : config.getArrows().values()) {
                            if (ar.getName().toLowerCase().startsWith(lArgs)) {
                                List<String> lore = ar.constructArrow(Arrays.copyOfRange(args, 1, args.length));
                                if (lore == null) {
                                    player.sendMessage(Storage.logo + ar.getCommand());
                                    return true;
                                }
                                ItemMeta soMeta = stack.getItemMeta();
                                soMeta.setLore(lore);
                                stack.setItemMeta(soMeta);
                                player.setItemInHand(stack);
                                player.sendMessage(Storage.logo + "Created " + ar.getName() + "s!");
                                return true;
                            }
                        }
                        player.sendMessage(Storage.logo + "That arrow does not exist!");
                }
                break;
        }
        return true;
    }
}

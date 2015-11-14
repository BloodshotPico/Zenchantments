package zedly.zenchantments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import static org.bukkit.GameMode.CREATIVE;
import org.bukkit.Location;
import org.bukkit.Material;
import static org.bukkit.Material.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import static zedly.zenchantments.Storage.rnd;

public class Utilities {

    public static ArrayList<ItemStack> getRelevant(Player p) {
        ArrayList<ItemStack> stk = new ArrayList<>();
        stk.addAll(Arrays.asList(p.getInventory().getArmorContents()));
        stk.add(p.getItemInHand());
        return stk;

    }

    public static void addUnbreaking(ItemStack is, int damage, Player p) {
        if (!p.getGameMode().equals(CREATIVE)) {
            for (int i = 0; i < damage; i++) {
                if (rnd.nextInt(100) <= (100 / (is.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DURABILITY) + 1))) {
                    is.setDurability((short) (is.getDurability() + 1));
                }
            }
            ItemStack hand = p.getItemInHand();
            p.setItemInHand(hand.getDurability() > hand.getType().getMaxDurability() ? null : hand);
        }
    }

    public static void addUnbreaking(Player player, ItemStack is, int damage) {
        if (!player.getGameMode().equals(CREATIVE)) {
            for (int i = 0; i < damage; i++) {
                if (rnd.nextInt(100) <= (100 / (is.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DURABILITY) + 1))) {
                    is.setDurability((short) (is.getDurability() + 1));
                }
            }
        }
    }

    public static void removeItem(Player player, Material mat, short data, int amount) {
        if (player.getGameMode().equals(CREATIVE)) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                if (inv.getItem(i) != null && inv.getItem(i).getType() == mat && inv.getItem(i).getDurability() == data) {
                    if (inv.getItem(i).getAmount() > amount) {
                        int res = inv.getItem(i).getAmount() - amount;
                        ItemStack rest = inv.getItem(i);
                        rest.setAmount(res);
                        inv.setItem(i, rest);
                        return;
                    } else {
                        amount -= inv.getItem(i).getAmount();
                        inv.setItem(i, null);
                    }
                }
            }
        }
    }

    public static void removeItem(Player player, Material mat, int amount) {
        removeItem(player, mat, (short) 0, amount);
    }

    public static void removeItem(Player player, ItemStack is) {
        removeItem(player, is.getType(), is.getDurability(), is.getAmount());
    }

    public static boolean removeItemCheck(Player player, Material mat, short data, int amount) {
        if (player.getGameMode().equals(CREATIVE)) {
            return true;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null && inv.getItem(i).getType() == mat && inv.getItem(i).getDurability() == data) {
                if (inv.getItem(i).getAmount() > amount) {
                    int res = inv.getItem(i).getAmount() - amount;
                    ItemStack rest = inv.getItem(i);
                    rest.setAmount(res);
                    inv.setItem(i, rest);
                    return true;
                } else {
                    amount -= inv.getItem(i).getAmount();
                    inv.setItem(i, null);
                    return true;
                }
            }
        }
        return false;
    }

    public static int getEnchantLevel(int maxlevel, int levels) {
        if (maxlevel == 1) {
            return 1;
        }
        int sectionsize = 32 / (maxlevel - 1);
        int position = levels / sectionsize;
        int mod = levels - position * sectionsize;
        if (Storage.rnd.nextInt(2 * sectionsize) >= mod) {
            return position + 1;
        } else {
            return position + 2;
        }
    }

    public static int getNumber(String numeral) {
        switch (numeral.toUpperCase()) {
            case "-":
                return 0;
            case "I":
                return 1;
            case "II":
                return 2;
            case "III":
                return 3;
            case "IV":
                return 4;
            case "V":
                return 5;
            case "VI":
                return 6;
            case "VII":
                return 7;
            case "VIII":
                return 8;
            case "IX":
                return 9;
            case "X":
                return 10;
            default:
                return 1;
        }
    }

    public static String getRomanString(int number) {
        switch (number) {
            case 0:
                return "-";
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return "I";
        }
    }

    public static String getRomanString(int number, int limit) {
        if (number > limit) {
            return getRomanString(limit);
        } else {
            return getRomanString(number);
        }
    }

    public static ArrayList<ItemStack> fortuneDrops(int level, Block blk) {
        Material mat = blk.getType();
        ArrayList<ItemStack> stacks = new ArrayList<>();
        int prob = Storage.rnd.nextInt(100);
        switch (mat) {
            case COAL_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
            case QUARTZ_ORE:
            case LAPIS_ORE:
                short n = 0;
                if (mat == LAPIS_ORE) {
                    n = 4;
                }
                Material m = AIR;
                int n0 = blk.getDrops().size();
                for (ItemStack s : blk.getDrops()) {
                    m = s.getType();
                }
                Random rnd = new Random();
                int c = 0;
                for (int i2 = 0; i2 < n0; i2++) {
                    double f = Math.pow(1.3, level);
                    f -= 1;
                    f *= 100;
                    int something = (int) f;
                    c++;
                    while (something > 0) {
                        if (rnd.nextInt(100) < something) {
                            c++;
                        }
                        something -= 100;
                    }
                }
                for (int i3 = 0; i3 < c; i3++) {
                    stacks.add(new ItemStack(m, 1, n));
                }
                break;
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
                int n1 = blk.getDrops().size() - 1;
                n1 += level;
                for (int i = 0; i < n1; i++) {
                    stacks.add(new ItemStack(REDSTONE, 1));
                }
                break;
            case SEA_LANTERN:
                int n2 = blk.getDrops().size() - 1;
                n2 += level;
                if (n2 > 5) {
                    n2 = 5;
                }
                for (int i = 0; i < n2; i++) {
                    stacks.add(new ItemStack(PRISMARINE_CRYSTALS, 1));
                }
                break;
            case GLOWSTONE:
                int n3 = blk.getDrops().size() - 1;
                n3 += level;
                if (n3 > 4) {
                    n3 = 4;
                }
                for (int i = 0; i < n3; i++) {
                    stacks.add(new ItemStack(GLOWSTONE_DUST, 1));
                }
                break;
            case GRAVEL:
                if (level == 1) {
                    if (prob < 15) {
                        ItemStack stk2 = new ItemStack(FLINT, 1);
                        stacks.add(stk2);
                    } else {
                        ItemStack stk2 = new ItemStack(GRAVEL, 1);
                        stacks.add(stk2);
                    }
                } else if (level == 2) {

                    if (prob < 25) {
                        ItemStack stk2 = new ItemStack(FLINT, 1);
                        stacks.add(stk2);
                    } else {
                        ItemStack stk2 = new ItemStack(GRAVEL, 1);
                        stacks.add(stk2);
                    }
                } else if (level >= 3) {
                    ItemStack stk2 = new ItemStack(FLINT, 1);
                    stacks.add(stk2);
                }
                break;
            default:
                stacks.addAll(blk.getDrops());
                break;
        }
        return stacks;
    }

    public static ArrayList<ItemStack> silktouchDrops(Block blk) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        Material mat = blk.getType();
        Material m;
        switch (mat) {
            case COAL_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
            case GRASS:
            case ICE:
            case PACKED_ICE:
            case LAPIS_ORE:
            case MYCEL:
            case QUARTZ_ORE:
            case GLASS:
            case SEA_LANTERN:
            case THIN_GLASS:
            case ENDER_CHEST:
            case VINE:
            case MELON_BLOCK:
            case GLOWSTONE:
            case CLAY:
            case SNOW_BLOCK:
            case BOOKSHELF:
            case DEAD_BUSH:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
                m = mat;
                ItemStack stk = new ItemStack(m, 1);
                stacks.add(stk);
                break;
            case STAINED_GLASS_PANE:
            case STAINED_GLASS:
            case DIRT:
            case STONE:
            case DOUBLE_PLANT:
            case LONG_GRASS:
            case LEAVES:
            case LEAVES_2:
                short s = (short) blk.getData();
                if (s >= 8 && (mat == LEAVES || mat == LEAVES_2)) {
                    s -= 8;
                }
                m = mat;
                ItemStack stk2 = new ItemStack(m, 1, s);
                stacks.add(stk2);
                break;
            case GLOWING_REDSTONE_ORE:
            case REDSTONE_ORE:
                ItemStack stk3 = new ItemStack(REDSTONE_ORE, 1);
                stacks.add(stk3);
                break;
            default:
                stacks.addAll(blk.getDrops());
                break;
        }
        return stacks;
    }

    public static Location getCenter(Location loc) {
        double x = loc.getX();
        double z = loc.getZ();
        if (x >= 0) {
            x += .5;
        } else {
            x += .5;
        }
        if (z >= 0) {
            z += .5;
        } else {
            z += .5;
        }
        Location lo = loc.clone();
        lo.setX(x);
        lo.setZ(z);
        return lo;
    }

    public static List<Entity> getNearbyEntities(Location loc, double x, double y, double z) {
        FallingBlock ent = loc.getWorld().spawnFallingBlock(loc, 132, (byte) 0);
        List<Entity> out = ent.getNearbyEntities(x, y, z);
        ent.remove();
        return out;
    }

    public static int grow(Block blk) {
        if (blk != null) {
            if (blk.getType() == COCOA) {
                if (blk.getData() < 8) {
                    blk.setData((byte) (blk.getData() + 4));
                    return 1;
                } else {
                    return 0;
                }
            } else if (blk.getType() == PUMPKIN_STEM || blk.getType() == MELON_STEM
                    || blk.getType() == CARROT || blk.getType() == CROPS || blk.getType() == POTATO) {
                if (blk.getData() < 7) {
                    blk.setData((byte) (blk.getData() + 1));
                    return 1;
                } else {
                    return 0;
                }
            } else if (blk.getType() == NETHER_WARTS) {
                if (blk.getData() < 3) {
                    blk.setData((byte) (blk.getData() + 1));
                    return 1;
                } else {
                    return 0;
                }
            } else if (blk.getType() == CACTUS) {
                return 0;
            } else if (blk.getType() == SUGAR_CANE_BLOCK) {
                return 0;
            }
        }
        return 0;
    }

    public static int getDirection(Player player) {
        float direction = player.getLocation().getYaw();
        int in = 0;
        if (direction < 0) {
            direction += 360;
        }
        direction %= 360;
        double i = (double) ((direction + 8) / 18);
        if (i >= 19 || i < 1) {//S
            in = 1;
        } else if (i < 3 && i >= 1) {//SW
            in = 2;
        } else if (i < 6 && i >= 3) {//W
            in = 3;
        } else if (i < 8 && i >= 6) {//NW
            in = 4;
        } else if (i < 11 && i >= 8) {//N
            in = 5;
        } else if (i < 13 && i >= 11) {//NE
            in = 6;
        } else if (i < 16 && i >= 13) {//E
            in = 7;
        } else if (i < 18 && i >= 16) {//SE
            in = 8;
        }
        return in;
    }

    public static int getSimpleDirection(float yaw, float pitch) {
        float direction = yaw;
        int in = 0;
        if (direction < 0) {
            direction += 360;
        }
        direction %= 360;
        double i = (double) ((direction + 8) / 18);
        if (i >= 18 || i < 3) {//S
            in = 1;
        } else if (i < 8 && i >= 3) {//W
            in = 2;
        } else if (i < 13 && i >= 8) {//N
            in = 3;
        } else if (i < 18 && i >= 13) {//E
            in = 4;
        }
        if (pitch < -50) {//U
            in = 5;
        } else if (pitch > 50) {//D
            in = 6;
        }
        return in;
    }

    public static void putArrow(Entity e, CustomArrow a, Player p) {
        HashSet<CustomArrow> ars;
        if (Storage.advancedProjectiles.containsKey(e)) {
            ars = Storage.advancedProjectiles.get(e);
        } else {
            ars = new HashSet<>();
        }
        ars.add(a);
        Storage.advancedProjectiles.put(e, ars);
        a.onLaunch(p, null);
    }

    public static boolean canUse(Player player, CustomEnchantment ench) {
        if (!player.hasPermission("zenchantments.enchant.use")) {
            return false;
        }
        if (Storage.playerSettings.containsKey(player.getUniqueId())) {
            if (Storage.playerSettings.get(player.getUniqueId()).contains(ench)) {
                return false;
            }
        }
        return true;
    }

    public static boolean eventStart(Player p, String e) {
        if (Storage.duringEvents.containsKey(p)) {
            if (Storage.duringEvents.get(p).contains(e)) {
                return true;
            } else {
                Storage.duringEvents.get(p).add(e);
                return false;
            }
        } else {
            HashSet<String> s = new HashSet<>();
            s.add(e);
            Storage.duringEvents.put(p, s);
            return false;
        }
    }

    public static void eventEnd(Player p, String e) {
        Storage.duringEvents.get(p).remove(e);
    }

    public static boolean canDamage(Entity damager, Entity entity) {
        if (!Storage.damagingPlayer.contains(damager)) {
            Storage.damagingPlayer.add(damager);
            EntityDamageByEntityEvent evt = new EntityDamageByEntityEvent(damager, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1);
            Bukkit.getPluginManager().callEvent(evt);
            Storage.damagingPlayer.remove(damager);
            if ((damager instanceof Player && !Config.get(damager.getWorld()).enchantPVP()) || !(entity instanceof LivingEntity)) {
                return false;
            }
            return !evt.isCancelled();
        }
        return false;
    }

    public static void addPotion(LivingEntity ent, PotionEffectType type, int length, int intensity) {
        for (PotionEffect eff : ent.getActivePotionEffects()) {
            if (eff.getType().equals(type)) {
                if (eff.getAmplifier() > intensity) {
                    return;
                } else {
                    if (eff.getDuration() > length) {
                        return;
                    } else {
                        ent.removePotionEffect(type);
                    }
                }
            }
        }
        ent.addPotionEffect(new PotionEffect(type, length, intensity));
    }

    public static boolean canEdit(Player player, Block block) {
        if (Storage.worldGuard == null) {
            return true;
        } else {
            return Storage.worldGuard.canBuild(player, block);
        }
    }

    public static byte place(Player player) {
        boolean vertical = Utilities.getSimpleDirection(0, player.getLocation().getPitch()) == 5;
        int horizontal = Utilities.getSimpleDirection(player.getLocation().getYaw(), 0);

        byte data = player.getItemInHand().getData().getData();
        switch (player.getItemInHand().getType()) {
            case WOOD_STEP:
                if (vertical) {
                    return (byte) (data + 5);
                }
        }
        return data;
    }

}

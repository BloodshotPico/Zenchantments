package zedly.zenchantments;

import java.util.*;
import org.bukkit.*;
import static org.bukkit.Material.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.*;
import static org.bukkit.potion.PotionEffectType.*;
import org.bukkit.util.Vector;

public class ElementalArrow implements AdvancedArrow {

    private final Projectile arrow;
    private int tick;

//Empty Methods
    public ElementalArrow() {
        this(null);
    }

    public ElementalArrow(Projectile arrow) {
        this.arrow = arrow;
    }

    public int getTick() {
        return tick;
    }

    public void tick() {
        tick++;
    }

    public Projectile getArrow() {
        return arrow;
    }

    public static Recipe getRecipe(ItemStack is) {
        return null;
    }

    public void onLaunch(LivingEntity player, List<String> lore) {
    }

    public void onFlight() {
    }

    public void onImpact() {
        die();
    }

    public void onKill(EntityDeathEvent evt) {
    }

    public boolean onImpact(EntityDamageByEntityEvent evt) {
        LivingEntity ent = (LivingEntity) evt.getEntity();
        onImpact();
        return true;
    }

    public String getName() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getCommand() {
        return null;
    }

    public List<String> constructArrow(String[] args) {
        return null;
    }

    public void die() {
        final Entity e = arrow;
        final ElementalArrow arrow2 = this;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Storage.zenchantments, new Runnable() {
            public void run() {
                if (Storage.advancedProjectiles.containsKey(e)) {
                    if (Storage.advancedProjectiles.get(e).size() == 1) {
                        Storage.advancedProjectiles.remove(e);
                    } else {
                        Storage.advancedProjectiles.get(e).remove(arrow2);
                    }
                }
            }
        }, 1);
    }

//Elemental Arrows
    public static class ArrowBouncifying extends ElementalArrow {

        private int duration;

        public ArrowBouncifying(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            if (lore.size() >= 2) {
                try {
                    duration = Integer.parseInt(lore.get(1));
                } catch (NumberFormatException ex) {
                    duration = 100;
                }
            } else {
                duration = 100;
            }
        }

        public void onImpact() {
            getArrow().getWorld().playEffect(getArrow().getLocation(), Effect.POTION_BREAK, 100);
            for (Entity ent : getArrow().getNearbyEntities(4, 3, 4)) {
                if (Utilities.canDamage((Entity) getArrow().getShooter(), ent)) {
                    Utilities.addPotion((LivingEntity) ent, JUMP, duration, 9);
                }
            }
            die();
        }

        public String getName() {
            return "Bouncifying Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(SLIME_BALL).addIngredient(FEATHER).addIngredient(SLIME_BALL).addIngredient(FEATHER);
        }

        public String getDescription() {
            return "Gives nearby targets jump boost";
        }

        public String getCommand() {
            return "/arrow bouncify " + ChatColor.GREEN + ChatColor.ITALIC + "<?duration>" + ChatColor.AQUA + " (default is 100)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Bouncifying Arrow");
            if (args.length == 0) {
                return lore;
            }
            try {
                Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[0]);
            return lore;
        }

    }

    public static class ArrowCommand extends ElementalArrow {

        public String command;
        public Player shooter;

        public ArrowCommand(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            if (!(player instanceof Player)) {
                die();
            }
            shooter = (Player) player;
            if (lore.size() == 2) {
                command = lore.get(1);
            } else {
                command = "/tell %t gg";
            }
        }

        public boolean onImpact(EntityDamageByEntityEvent evt) {
            LivingEntity ent = (LivingEntity) evt.getEntity();
            if (ent instanceof Player) {
                Player player = (Player) ent;
                shooter.chat(command.replace("%t", player.getName()));
            }
            return false;
        }

        public String getName() {
            return "Command Arrow";
        }

        public String getDescription() {
            return "When the arrow hits a player, the shooter runs a command. The player's name can be retrieved using %t.";
        }

        public String getCommand() {
            return "/arrow command " + ChatColor.GREEN + "/command";
        }

        public List<String> constructArrow(String[] args) {
            if (args.length == 0) {
                return null;
            }
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.RED + "Command Arrow");
            String comm = args[0];
            for (int i = 1; i < args.length; i++) {
                comm += " " + args[i];
            }
            lore.add(comm);
            return lore;
        }
    }

    public static class ArrowDerp extends ElementalArrow {

        boolean humans;

        public ArrowDerp(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            humans = lore.size() == 2 && lore.get(1).equals("unsafe");
        }

        public void onImpact() {
            final LinkedList<LivingEntity> entities = new LinkedList<>();
            for (Entity ent : getArrow().getNearbyEntities(5, 5, 5)) {
                if (!(ent instanceof Player) || humans) {
                    if (Utilities.canDamage((Entity) getArrow().getShooter(), ent)) {
                        entities.add((LivingEntity) ent);
                        Storage.derpingEntities.add((LivingEntity) ent);
                    }
                }
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(Storage.zenchantments, new Runnable() {

                public void run() {
                    Storage.derpingEntities.removeAll(entities);
                }
            }, 300);
            die();
        }

        public String getName() {
            return "Derp Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(SUGAR).addIngredient(SUGAR).addIngredient(SUGAR).addIngredient(SUGAR).addIngredient(SUGAR);
        }

        public String getDescription() {
            return "Causes targets to derp around temporarily";
        }

        public String getCommand() {
            return "/arrow derp " + ChatColor.GREEN + ChatColor.ITALIC + "<?unsafe>" + ChatColor.AQUA + " (include to make it affect players)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Derp Arrow");
            if (args.length == 0) {
                return lore;
            }
            if (args[0].equalsIgnoreCase("unsafe")) {
                lore.add("unsafe");
            }
            return lore;
        }
    }

    public static class ArrowEuphoria extends ElementalArrow {

        private LivingEntity shooter;
        private int duration;

        public ArrowEuphoria(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            shooter = player;
            if (lore.size() >= 2) {
                try {
                    duration = Integer.parseInt(lore.get(1));
                } catch (NumberFormatException ex) {
                    duration = 400;
                }
            } else {
                duration = 400;
            }
        }

        public boolean onImpact(EntityDamageByEntityEvent evt) {
            LivingEntity ent = (LivingEntity) evt.getEntity();
            Utilities.addPotion(shooter, SPEED, duration, 2);
            Utilities.addPotion(shooter, JUMP, duration, 2);
            Utilities.addPotion(shooter, REGENERATION, duration, 0);
            Utilities.addPotion(shooter, FAST_DIGGING, duration, 2);
            Utilities.addPotion(shooter, INCREASE_DAMAGE, duration, 2);
            return true;
        }

        public String getName() {
            return "Euphoria Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(GHAST_TEAR);
        }

        public String getDescription() {
            return "Gives the shooter several positive effects upon hitting a target";
        }

        public String getCommand() {
            return "/arrow euphoria " + ChatColor.GREEN + ChatColor.ITALIC + "<?duration>" + ChatColor.AQUA + " (default is 400)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Euphoria Arrow");
            if (args.length == 0) {
                return lore;
            }
            try {
                Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[0]);
            return lore;
        }

    }

    public static class ArrowExploding extends ElementalArrow {

        private float radius;

        public ArrowExploding(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            if (lore.size() >= 2) {
                try {
                    radius = Float.parseFloat(lore.get(1));
                } catch (NumberFormatException ex) {
                    radius = 2.5F;
                }
            } else {
                radius = 2.5F;
            }
        }

        public void onImpact() {
            Location l = getArrow().getLocation();
            getArrow().getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), radius, false, false);
            getArrow().remove();
            die();
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(TNT);
        }

        public String getName() {
            return "Exploding Arrow";
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public String getDescription() {
            return "Creates an explosion upon impact";
        }

        public String getCommand() {
            return "/arrow exploding " + ChatColor.GREEN + ChatColor.ITALIC + "<?power>" + ChatColor.AQUA + " (default is 2.5)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Exploding Arrow");
            if (args.length == 0) {
                return lore;
            }
            try {
                Double.parseDouble(args[0]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[0]);
            return lore;
        }
    }

    public static class ArrowFast extends ElementalArrow {

        private float speed;

        public ArrowFast(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            if (lore != null && lore.size() >= 2) {
                try {
                    speed = Float.parseFloat(lore.get(1));
                } catch (NumberFormatException | NullPointerException e) {
                    speed = 3.5F;
                }
            } else {
                speed = 3.5F;
            }
            getArrow().setVelocity(getArrow().getVelocity().normalize().multiply(speed));
            die();
        }

        public String getName() {
            return "Fast Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(FEATHER);
        }

        public String getDescription() {
            return "Allows the shooter to quickly draw and shoot arrows at full force";
        }

        public String getCommand() {
            return "/arrow fast " + ChatColor.GREEN + ChatColor.ITALIC + "<?speed>" + ChatColor.AQUA + " (default is 3.5)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Fast Arrow");
            if (args.length == 0) {
                return lore;
            }
            try {
                Double.parseDouble(args[0]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[0]);
            return lore;
        }

    }

    public static class ArrowGrenade extends ElementalArrow {

        private int balls;
        private float radius;

        public ArrowGrenade(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            if (lore.size() >= 2) {
                try {
                    balls = Integer.parseInt(lore.get(1));
                } catch (NumberFormatException ex) {
                    balls = 8;
                }
            } else {
                balls = 8;
            }
            if (lore.size() >= 3) {
                try {
                    radius = Float.parseFloat(lore.get(2));
                } catch (NumberFormatException ex) {
                    radius = 1.5F;
                }
            } else {
                radius = 1.5F;
            }
        }

        public void onImpact() {
            for (int i = 0; i < balls; i++) {
                Snowball ball = (Snowball) getArrow().getWorld().spawnEntity(getArrow().getLocation(), EntityType.SNOWBALL);
                ball.setVelocity(new Vector(Storage.rnd.nextGaussian(), Storage.rnd.nextGaussian(), Storage.rnd.nextGaussian()).normalize().add(new Vector(0, 0.75, 0)).normalize().multiply(0.5));
                ArrowExploding ar = new ArrowExploding(ball);
                ar.setRadius(radius);
                Set<AdvancedArrow> a = new HashSet<>();
                a.add(ar);
                Storage.advancedProjectiles.put(ball, a);
            }
            die();
        }

        public String getName() {
            return "Grenade Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(TNT).addIngredient(BLAZE_POWDER).addIngredient(BLAZE_POWDER).addIngredient(BLAZE_POWDER).addIngredient(SNOW_BALL).addIngredient(SNOW_BALL);
        }

        public String getDescription() {
            return "Creates an explosion with shrapnel upon impact";
        }

        public String getCommand() {
            return "/arrow grenade " + ChatColor.GREEN + ChatColor.ITALIC + "<?fragments>" + ChatColor.AQUA + " (default is 8) " + ChatColor.GREEN + ChatColor.ITALIC + "<?power>" + ChatColor.AQUA + " (default is 1.5)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Grenade Arrow");
            if (args.length == 0) {
                return lore;
            }
            try {
                Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[0]);
            if (args.length == 1) {
                return lore;
            }
            try {
                Double.parseDouble(args[1]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[1]);
            return lore;
        }
    }

    public static class ArrowHarpoon extends ElementalArrow {

        private LivingEntity shooter;
        private boolean humans;

        public ArrowHarpoon(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            shooter = player;
            humans = lore.size() >= 2 && lore.get(1).equals("unsafe");
        }

        public boolean onImpact(EntityDamageByEntityEvent evt) {
            LivingEntity ent = (LivingEntity) evt.getEntity();
            if (humans || !(ent instanceof Player)) {
                if (Utilities.canDamage(shooter, evt.getEntity())) {
                    ent.teleport(shooter);
                }
            }
            return false;
        }

        public String getName() {
            return "Harpoon Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(STRING).addIngredient(FLINT);
        }

        public String getDescription() {
            return "Drags targets towards the shooter when hit";
        }

        public String getCommand() {
            return "/arrow harpoon " + ChatColor.GREEN + ChatColor.ITALIC + "<?unsafe>" + ChatColor.AQUA + " (include to make it affect players)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Harpoon Arrow");
            if (args.length == 0) {
                return lore;
            }
            if (args[0].equalsIgnoreCase("unsafe")) {
                lore.add("unsafe");
            }
            return lore;
        }

    }

    public static class ArrowIce extends ElementalArrow {

        private int duration;

        public ArrowIce(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            if (lore.size() >= 2) {
                try {
                    duration = Integer.parseInt(lore.get(1));
                } catch (NumberFormatException ex) {
                    duration = 100;
                }
            } else {
                duration = 100;
            }
        }

        public void onImpact() {
            getArrow().getWorld().playEffect(getArrow().getLocation(), Effect.POTION_BREAK, 100);
            for (Entity ent : getArrow().getNearbyEntities(4, 3, 4)) {
                if (Utilities.canDamage((Entity) getArrow().getShooter(), ent)) {
                    Utilities.addPotion((LivingEntity) ent, SLOW, duration, 50);
                }
            }
            die();
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(ICE).addIngredient(ICE).addIngredient(ICE);
        }

        public String getName() {
            return "Ice Arrow";
        }

        public String getDescription() {
            return "Freezes targets temporarily";
        }

        public String getCommand() {
            return "/arrow ice " + ChatColor.GREEN + ChatColor.ITALIC + "<?duration>" + ChatColor.AQUA + " (default is 100)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Ice Arrow");
            if (args.length == 0) {
                return lore;
            }
            try {
                Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[0]);
            return lore;
        }
    }

    public static class ArrowLeech extends ElementalArrow {

        private boolean humans;
        private LivingEntity shooter;

        public ArrowLeech(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            humans = lore.size() >= 2 && lore.get(1).equals("unsafe");
            shooter = player;
        }

        public boolean onImpact(EntityDamageByEntityEvent evt) {
            LivingEntity ent = (LivingEntity) evt.getEntity();
            if (!(ent instanceof Player) || humans) {
                if (Utilities.canDamage((Entity) getArrow().getShooter(), evt.getEntity())) {
                    if (ent.getHealth() >= 2 && shooter.getHealth() <= 18) {
                        ent.damage(2);
                        shooter.setHealth(shooter.getHealth() + 2);
                        Location playLoc = ent.getLocation();
                        Location entLoc = shooter.getLocation();
                        Location total = playLoc.subtract(entLoc);
                        Vector vect = new Vector(total.getX(), total.getY(), total.getZ());
                        vect.multiply(.465);
                        ent.setVelocity(vect);
                        return false;
                    }
                }
            }
            return true;
        }

        public String getName() {
            return "Leech Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(REDSTONE).addIngredient(REDSTONE).addIngredient(REDSTONE).addIngredient(REDSTONE);
        }

        public String getDescription() {
            return "Gives the shooter some of the target's health";
        }

        public String getCommand() {
            return "/arrow leech " + ChatColor.GREEN + ChatColor.ITALIC + "<?unsafe>" + ChatColor.AQUA + " (include to make it affect players)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Leech Arrow");
            if (args.length == 0) {
                return lore;
            }
            if (args[0].equalsIgnoreCase("unsafe")) {
                lore.add("unsafe");
            }
            return lore;
        }

    }

    public static class ArrowLightning extends ElementalArrow {

        public boolean unsafe;

        public ArrowLightning(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            if (lore.size() == 1 || lore.get(1).equals("unsafe")) {
                unsafe = true;
            }
        }

        public void onImpact() {
            final Entity lightning = getArrow().getWorld().strikeLightning(getArrow().getLocation());
            Storage.lightnings.add(lightning);
            if (!unsafe) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Storage.zenchantments, new Runnable() {

                    public void run() {
                        Storage.lightnings.remove(lightning);
                    }
                }, 2);
            }
            die();
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(BLAZE_ROD).addIngredient(BLAZE_ROD);
        }

        public String getName() {
            return "Lightning Arrow";
        }

        public String getDescription() {
            return "Strikes lightning upon impact";
        }

        public String getCommand() {
            return "/arrow lightning " + ChatColor.GREEN + ChatColor.ITALIC + "<?unsafe>" + ChatColor.AQUA + " (include to make the lightning zap pigs and creepers)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Lightning Arrow");
            if (args.length == 0) {
                return lore;
            }
            if (args[0].equalsIgnoreCase("unsafe")) {
                lore.add("unsafe");
            }
            return lore;
        }

    }

    public static class ArrowPoisoned extends ElementalArrow {

        private int duration;

        public ArrowPoisoned(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            if (lore.size() >= 2) {
                try {
                    duration = Integer.parseInt(lore.get(1));
                } catch (NumberFormatException ex) {
                    duration = 50;
                }
            } else {
                duration = 50;
            }
        }

        public void onImpact() {
            getArrow().getWorld().playEffect(getArrow().getLocation(), Effect.POTION_BREAK, 100);
            for (Entity ent : getArrow().getNearbyEntities(4, 3, 4)) {
                if (Utilities.canDamage((Entity) getArrow().getShooter(), ent)) {
                    Utilities.addPotion((LivingEntity) ent, POISON, duration, 1);
                }
            }
            die();
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(FERMENTED_SPIDER_EYE).addIngredient(FERMENTED_SPIDER_EYE);
        }

        public String getName() {
            return "Poisoned Arrow";
        }

        public String getDescription() {
            return "Poisons targets temporarily";
        }

        public String getCommand() {
            return "/arrow poison " + ChatColor.GREEN + ChatColor.ITALIC + "<?power>" + ChatColor.AQUA + " (default is 50)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Poisoned Arrow");
            if (args.length == 0) {
                return lore;
            }
            try {
                Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[0]);
            return lore;
        }

    }

    public static class ArrowSupersonic extends ElementalArrow {

        private LivingEntity shooter;
        private int duration;

        public ArrowSupersonic(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            shooter = player;
            if (lore.size() >= 2) {
                try {
                    duration = Integer.parseInt(lore.get(1));
                } catch (NumberFormatException ex) {
                    duration = 400;
                }
            } else {
                duration = 400;
            }
        }

        public boolean onImpact(EntityDamageByEntityEvent evt) {
            LivingEntity ent = (LivingEntity) evt.getEntity();
            Utilities.addPotion(shooter, SPEED, duration, 3);
            return true;
        }

        public String getName() {
            return "Supersonic Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(SUGAR).addIngredient(NETHER_STALK).addIngredient(GLOWSTONE_DUST).addIngredient(GOLD_NUGGET).addIngredient(GOLD_NUGGET);
        }

        public String getDescription() {
            return "Gives the shooter a speed boost if the arrow hits a mob or player";
        }

        public String getCommand() {
            return "/arrow supersonic " + ChatColor.GREEN + ChatColor.ITALIC + "<?duration>" + ChatColor.AQUA + " (default is 400)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Supersonic Arrow");
            if (args.length == 0) {
                return lore;
            }
            try {
                Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                return null;
            }
            lore.add(args[0]);
            return lore;
        }
    }

    public static class ArrowTeleport extends ElementalArrow {

        private LivingEntity shooter;
        private boolean hurt;

        public ArrowTeleport(Projectile entity) {
            super(entity);
        }

        public void onLaunch(LivingEntity player, List<String> lore) {
            shooter = player;
            hurt = lore.size() == 1 || !lore.get(1).equals("safe");
        }

        public void onImpact() {
            shooter.teleport(getArrow());
            if (hurt) {
                shooter.damage(5);
            }
            die();
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(ENDER_PEARL).addIngredient(ENDER_PEARL);
        }

        public String getName() {
            return "Teleport Arrow";
        }

        public String getDescription() {
            return "Teleports the shooter wherever the arrow lands";
        }

        public String getCommand() {
            return "/arrow teleport " + ChatColor.GREEN + ChatColor.ITALIC + "<?safe>" + ChatColor.AQUA + " (include to make user take no damage when used)";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Teleport Arrow");
            if (args.length == 0) {
                return lore;
            }
            if (args[0].equalsIgnoreCase("safe")) {
                lore.add("safe");
            }
            return lore;
        }

    }

    public static class ArrowWeb extends ElementalArrow {

        public ArrowWeb(Projectile entity) {
            super(entity);
        }

        public void onImpact() {
            int x = getArrow().getLocation().getBlockX();
            int y = getArrow().getLocation().getBlockY();
            int z = getArrow().getLocation().getBlockZ();
            for (int i_x = x - 2; i_x < x + 3; i_x++) {
                for (int i_y = y - 1; i_y < y + 2; i_y++) {
                    for (int i_z = z - 2; i_z < z + 3; i_z++) {
                        if (getArrow().getWorld().getBlockAt(i_x, i_y, i_z).getType() == AIR && Storage.rnd.nextInt(3) == 0) {
                            if (Config.get(getArrow().getWorld()).enchantPVP()) {
                                getArrow().getWorld().getBlockAt(i_x, i_y, i_z).setType(WEB);
                                Storage.webs.add(getArrow().getWorld().getBlockAt(i_x, i_y, i_z));
                            }
                        }
                    }
                }
            }
            die();
        }

        public String getName() {
            return "Web Arrow";
        }

        public static Recipe getRecipe(ItemStack is) {
            return new ShapelessRecipe(is).addIngredient(ARROW).addIngredient(STRING).addIngredient(STRING).addIngredient(STRING).addIngredient(STRING).addIngredient(STRING).addIngredient(STRING).addIngredient(STRING).addIngredient(STRING);
        }

        public String getDescription() {
            return "Encases targets in spiderwebs";
        }

        public String getCommand() {
            return "/arrow web";
        }

        public List<String> constructArrow(String[] args) {
            LinkedList<String> lore = new LinkedList<>();
            lore.add(ChatColor.AQUA + "Web Arrow");
            return lore;
        }

    }

}

package com.daniel.blocksumo.events;

import com.daniel.blocksumo.Main;
import com.daniel.blocksumo.manager.MatchManager;
import com.daniel.blocksumo.menu.Menu;
import com.daniel.blocksumo.model.Match;
import com.daniel.blocksumo.objects.enums.MatchState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ItensEvents implements Listener {

    private final MatchManager manager;

    public ItensEvents(MatchManager manager) {
        this.manager = manager;
    }

    @EventHandler // TNT explosion
    public void onExplosion(EntityExplodeEvent e) {
        Entity entity = e.getEntity();
        Match match = manager.findMatchInGameByWorld(entity.getWorld());
        if (match == null) return;
        if (entity.getType() == EntityType.PRIMED_TNT) {
            e.setCancelled(true);

            Location locationExplosion = entity.getLocation();
            World world = locationExplosion.getWorld();

            for (Entity nearby : world.getNearbyEntities(locationExplosion, 5, 5, 5)) {
                if (nearby != entity && nearby instanceof Player) {
                    Player player = (Player) nearby;
                    if (!match.getSpectators().contains(player.getUniqueId())) {
                        Vector vector = nearby.getLocation().toVector().subtract(locationExplosion.toVector()).normalize().multiply(3);
                        nearby.setVelocity(vector);
                    }
                }
            }
            for (Block block : e.blockList()) {
                if (block.getType() == Material.WOOL && (!match.getGenerator().isOriginalBlock(block))) {
                    block.setType(Material.AIR);
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    locationExplosion.getBlock().setType(Material.AIR);
                }
            }.runTaskLater(Main.getPlugin(Main.class), 2);
        }
    }

    @EventHandler
    public void onInteractWithFireball(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        Match match = manager.findMatchByPlayer(player);

        if (match == null) return;
        if (match.getSpectators().contains(player.getUniqueId())) return;

        ItemStack item = e.getItem();
        if (match.getState() == MatchState.STARTED) {
            if (item == null) return;
            if (item.getType() == Material.FIREBALL) {
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

                    Fireball fireball = player.launchProjectile(Fireball.class);
                    fireball.setIsIncendiary(false);
                    fireball.setYield(0);
                    Vector direction = player.getLocation().getDirection();
                    fireball.setVelocity(direction.multiply(2));

                    int amount = item.getAmount();
                    if (amount > 1) {
                        item.setAmount(amount - 1);
                    } else {
                        player.getInventory().remove(item);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectile(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        if (projectile instanceof Fireball) {
            Fireball fireball = (Fireball) projectile;
            if (fireball.getShooter() instanceof Player) {
                fireball.setYield(0);

                Player shooter = (Player) fireball.getShooter();
                Match match = manager.findMatchByPlayer(shooter);
                if (match == null || match.getSpectators().contains(shooter.getUniqueId())) {
                    return;
                }


                ((Fireball) projectile).setIsIncendiary(false);
                Location impactLocation = projectile.getLocation();

                int radius = 3;
                int minX = impactLocation.getBlockX() - radius;
                int minY = impactLocation.getBlockY() - radius;
                int minZ = impactLocation.getBlockZ() - radius;
                int maxX = impactLocation.getBlockX() + radius;
                int maxY = impactLocation.getBlockY() + radius;
                int maxZ = impactLocation.getBlockZ() + radius;

                World world = impactLocation.getWorld();

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            Block block = world.getBlockAt(x, y, z);
                            if (block.getType() == Material.WOOL && (!match.getGenerator().isOriginalBlock(block))) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }
}

package pl.lunarhost.lunaridledetector;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LunarIdleDetectorPlugin extends JavaPlugin implements Listener {

    private final HashMap<UUID, Long> lastActivity = new HashMap<>();
    private long kickTime;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        startAFKCheckTask();
    }

    private void loadConfig() {
        kickTime = getConfig().getLong("kick-time") * 60 * 1000;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    private void startAFKCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (lastActivity.containsKey(uuid)) {
                        long lastActive = lastActivity.get(uuid);
                        if (now - lastActive > kickTime) {
                            String kickMessage = getConfig().getString("kick-message");
                            String formattedMessage = kickMessage.replace("%time%", String.valueOf(kickTime / 60000));
                            player.kickPlayer(formattedMessage);
                            lastActivity.remove(uuid);
                        }
                    } else {
                        lastActivity.put(uuid, now);
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L * 60);
    }
}

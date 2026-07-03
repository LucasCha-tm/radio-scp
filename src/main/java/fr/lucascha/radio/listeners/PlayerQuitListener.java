package fr.lucascha.radio.listeners;

import fr.lucascha.radio.managers.RadioManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        RadioManager.getInstance().removePlayer(event.getPlayer());
    }
}

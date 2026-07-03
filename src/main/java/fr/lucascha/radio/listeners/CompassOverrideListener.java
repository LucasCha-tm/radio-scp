package fr.lucascha.radio.listeners;

import fr.lucascha.radio.items.TalkieWalkieItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CompassOverrideListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;
        var item = event.getItem();
        if (item == null) return;
        if (TalkieWalkieItem.isTalkieWalkie(item)) {
            event.setCancelled(true);
        }
    }
}

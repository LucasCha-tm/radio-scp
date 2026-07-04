package fr.lucascha.radio.listeners;

import fr.lucascha.radio.frequencies.Frequency;
import fr.lucascha.radio.items.TalkieWalkieItem;
import fr.lucascha.radio.managers.FrequencyMenuManager;
import fr.lucascha.radio.managers.RadioManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class TalkieWalkieListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!TalkieWalkieItem.isTalkieWalkie(item)) return;

        event.setCancelled(true);
        FrequencyMenuManager.openMenu(player, TalkieWalkieItem.getFrequency(item));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        if (!title.contains("Choisir une Frequence")) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        // Bouton fermer (RED_CONCRETE slot 22)
        if (clicked.getType() == Material.RED_CONCRETE) {
            player.closeInventory();
            return;
        }

        Frequency freq = FrequencyMenuManager.getFrequencyFromMenuItem(clicked);
        if (freq == null) return;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (!TalkieWalkieItem.isTalkieWalkie(handItem)) {
            handItem = player.getInventory().getItemInOffHand();
        }

        RadioManager.getInstance().setFrequency(player, freq, handItem);
    }
}

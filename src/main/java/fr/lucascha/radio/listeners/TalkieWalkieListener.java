package fr.lucascha.radio.listeners;

import fr.lucascha.radio.frequencies.Frequency;
import fr.lucascha.radio.items.TalkieWalkieItem;
import fr.lucascha.radio.managers.FrequencyMenuManager;
import fr.lucascha.radio.managers.RadioManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

    // Clic droit avec le talkie en main → ouvre le menu
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

    // Clic dans le menu → change la fréquence
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Vérifier que c'est le menu radio via le titre
        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        if (!title.contains("Choisir une Frequence")) return;

        event.setCancelled(true);

        // Ignorer les clics dans l'inventaire du joueur
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Frequency freq = FrequencyMenuManager.getFrequencyFromMenuItem(clicked);
        if (freq == null) return;

        // Récupérer le talkie en main
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (!TalkieWalkieItem.isTalkieWalkie(handItem)) {
            handItem = player.getInventory().getItemInOffHand();
        }

        RadioManager.getInstance().setFrequency(player, freq, handItem);
    }
}

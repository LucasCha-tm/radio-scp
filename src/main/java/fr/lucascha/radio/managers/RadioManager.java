package fr.lucascha.radio.managers;

import fr.lucascha.radio.RadioPlugin;
import fr.lucascha.radio.frequencies.Frequency;
import fr.lucascha.radio.items.TalkieWalkieItem;
import fr.lucascha.radio.listeners.HandCheckListener;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RadioManager {

    private static RadioManager instance;
    private final Map<UUID, Frequency> playerFrequencies = new HashMap<>();
    private HandCheckListener handCheckListener;

    public static RadioManager getInstance() {
        if (instance == null) instance = new RadioManager();
        return instance;
    }

    public void setHandCheckListener(HandCheckListener listener) {
        this.handCheckListener = listener;
    }

    public boolean setFrequency(Player player, Frequency frequency, ItemStack handItem) {
        String prefix = RadioPlugin.getInstance().getConfig().getString("messages.prefix", "§8[§6Radio§8] §r");

        if (!player.hasPermission(frequency.getPermission())) {
            player.sendMessage(Component.text(prefix + "§cAcces refuse a cette frequence."));
            if (frequency.isRestricted()) {
                player.sendMessage(Component.text(prefix + "§4§lFREQUENCE RESTREINTE - Permission requise."));
            }
            player.closeInventory();
            return false;
        }

        Frequency current = playerFrequencies.get(player.getUniqueId());
        if (frequency == current) {
            player.sendMessage(Component.text(prefix + "§eVous etes deja sur cette frequence."));
            player.closeInventory();
            return false;
        }

        playerFrequencies.put(player.getUniqueId(), frequency);

        // Mettre à jour l'item en main
        if (TalkieWalkieItem.isTalkieWalkie(handItem)) {
            TalkieWalkieItem.updateFrequency(handItem, frequency);
        }

        // Forcer la vérification de main immédiatement
        // (connecte au vocal si le talkie est bien en main)
        if (handCheckListener != null) {
            handCheckListener.checkHand(player);
        }

        player.sendMessage(Component.text(prefix + "§aFrequence : " + frequency.getColoredName()));
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.5f);
        player.closeInventory();
        return true;
    }

    public void removePlayer(Player player) {
        playerFrequencies.remove(player.getUniqueId());
        VoiceChatManager.getInstance().onPlayerQuit(player);
        if (handCheckListener != null) handCheckListener.removePlayer(player);
    }

    public Frequency getFrequency(Player player) {
        return playerFrequencies.get(player.getUniqueId());
    }

    public boolean isOnFrequency(Player player) {
        return playerFrequencies.containsKey(player.getUniqueId());
    }

    public long countPlayersOnFrequency(Frequency frequency) {
        return playerFrequencies.values().stream().filter(f -> f == frequency).count();
    }
}

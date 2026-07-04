package fr.lucascha.radio.listeners;

import fr.lucascha.radio.RadioPlugin;
import fr.lucascha.radio.frequencies.Frequency;
import fr.lucascha.radio.items.TalkieWalkieItem;
import fr.lucascha.radio.managers.RadioManager;
import fr.lucascha.radio.managers.VoiceChatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HandCheckListener implements Listener {

    /**
     * Fréquence sur laquelle le joueur est actuellement connecté vocalement.
     * Null = pas dans un groupe vocal.
     */
    private final Map<UUID, Frequency> voiceFrequency = new HashMap<>();

    /** Joueurs dont le micro est actif (talkie en main). */
    private final Map<UUID, Boolean> micActive = new HashMap<>();

    private BukkitTask checkTask;

    public void startTask() {
        checkTask = Bukkit.getScheduler().runTaskTimer(RadioPlugin.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkHand(player);
            }
        }, 20L, 20L);
    }

    public void stopTask() {
        if (checkTask != null) checkTask.cancel();
    }

    /**
     * Logique principale appelée à chaque tick ou event.
     *
     * Cas 1 : pas de fréquence → quitter le groupe vocal si on y était
     * Cas 2 : fréquence changée → re-rejoindre le nouveau groupe
     * Cas 3 : bonne fréquence, talkie en main → micro ON
     * Cas 4 : bonne fréquence, talkie PAS en main → micro OFF (écoute seulement)
     */
    public void checkHand(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        boolean holdingTalkie = TalkieWalkieItem.isTalkieWalkie(mainHand);
        Frequency freq = RadioManager.getInstance().getFrequency(player);
        Frequency currentVoiceFreq = voiceFrequency.get(uuid);

        if (freq == null) {
            // Pas de fréquence → quitter le groupe
            if (currentVoiceFreq != null) {
                VoiceChatManager.getInstance().leaveGroup(player);
                voiceFrequency.remove(uuid);
                micActive.remove(uuid);
            }
            return;
        }

        // Fréquence a changé (ou pas encore rejoint) → rejoindre le bon groupe
        if (!freq.equals(currentVoiceFreq)) {
            boolean joined = VoiceChatManager.getInstance().joinFrequency(player, freq);
            if (joined) {
                voiceFrequency.put(uuid, freq);
                if (holdingTalkie) {
                    VoiceChatManager.getInstance().unblockMic(player);
                    micActive.put(uuid, true);
                } else {
                    VoiceChatManager.getInstance().blockMic(player);
                    micActive.put(uuid, false);
                }
            }
            return;
        }

        // Bonne fréquence déjà active : mettre à jour le micro selon la main
        boolean wasMicActive = Boolean.TRUE.equals(micActive.get(uuid));
        if (holdingTalkie && !wasMicActive) {
            VoiceChatManager.getInstance().unblockMic(player);
            micActive.put(uuid, true);
        } else if (!holdingTalkie && wasMicActive) {
            VoiceChatManager.getInstance().blockMic(player);
            micActive.put(uuid, false);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Bukkit.getScheduler().runTask(RadioPlugin.getInstance(),
                () -> checkHand(event.getPlayer()));
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Bukkit.getScheduler().runTask(RadioPlugin.getInstance(),
                () -> checkHand(event.getPlayer()));
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Bukkit.getScheduler().runTask(RadioPlugin.getInstance(),
                () -> checkHand(event.getPlayer()));
    }

    /** Réinitialise l'état vocal pour forcer un changement de groupe au prochain checkHand. */
    public void resetVoiceState(Player player) {
        UUID uuid = player.getUniqueId();
        voiceFrequency.remove(uuid);
        micActive.remove(uuid);
        // Quitter le groupe actuel proprement
        VoiceChatManager.getInstance().leaveGroup(player);
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        voiceFrequency.remove(uuid);
        micActive.remove(uuid);
    }
}

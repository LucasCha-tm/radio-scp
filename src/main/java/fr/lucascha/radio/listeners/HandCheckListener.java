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

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * Gère la connexion au groupe vocal selon si le talkie est en main ou non.
 *
 * Comportement :
 * - Talkie en main + fréquence sélectionnée → rejoint le groupe vocal, micro activé.
 * - Talkie retiré de la main mais fréquence toujours active → reste dans le groupe
 *   (entend les autres) mais micro coupé (ne peut pas parler).
 * - Fréquence retirée (ou déconnexion) → quitte complètement le groupe.
 */
public class HandCheckListener implements Listener {

    /** Joueurs actuellement dans un groupe vocal (entendent les autres). */
    private final Set<UUID> inVoice = new HashSet<>();

    /** Joueurs dont le micro est actif (talkie en main). */
    private final Set<UUID> micActive = new HashSet<>();

    private BukkitTask checkTask;

    public void startTask() {
        // Vérifie toutes les secondes (20 ticks)
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
     * Logique principale :
     * - A une fréquence + talkie en main  → groupe vocal + micro ON
     * - A une fréquence + talkie PAS en main → groupe vocal + micro OFF (entend seulement)
     * - Pas de fréquence                  → quitte le groupe vocal
     */
    public void checkHand(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        boolean holdingTalkie = TalkieWalkieItem.isTalkieWalkie(mainHand);
        Frequency freq = RadioManager.getInstance().getFrequency(player);
        UUID uuid = player.getUniqueId();

        if (freq != null) {
            // Le joueur a une fréquence active
            if (!inVoice.contains(uuid)) {
                // Rejoindre le groupe (le joinFrequency active aussi le micro)
                boolean joined = VoiceChatManager.getInstance().joinFrequency(player, freq);
                if (joined) {
                    inVoice.add(uuid);
                    if (holdingTalkie) {
                        micActive.add(uuid);
                    } else {
                        // Rejoint le groupe mais micro coupé immédiatement
                        VoiceChatManager.getInstance().blockMic(player);
                    }
                }
            } else {
                // Déjà dans le groupe : mettre à jour l'état du micro selon la main
                if (holdingTalkie && !micActive.contains(uuid)) {
                    // Vient de reprendre le talkie en main → micro ON
                    VoiceChatManager.getInstance().unblockMic(player);
                    micActive.add(uuid);
                } else if (!holdingTalkie && micActive.contains(uuid)) {
                    // Vient de lâcher le talkie → micro OFF (reste dans le groupe)
                    VoiceChatManager.getInstance().blockMic(player);
                    micActive.remove(uuid);
                }
            }
        } else {
            // Pas de fréquence → quitter complètement le groupe
            if (inVoice.contains(uuid)) {
                VoiceChatManager.getInstance().leaveGroup(player);
                inVoice.remove(uuid);
                micActive.remove(uuid);
            }
        }
    }

    /** Quand le joueur change de slot actif. */
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Bukkit.getScheduler().runTask(RadioPlugin.getInstance(),
                () -> checkHand(event.getPlayer()));
    }

    /** Quand le joueur échange main/off-hand. */
    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Bukkit.getScheduler().runTask(RadioPlugin.getInstance(),
                () -> checkHand(event.getPlayer()));
    }

    /** Quand le joueur drop un item (peut être le talkie). */
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Bukkit.getScheduler().runTask(RadioPlugin.getInstance(),
                () -> checkHand(event.getPlayer()));
    }

    /** Retire le joueur des sets quand il se déconnecte. */
    public void removePlayer(Player player) {
        inVoice.remove(player.getUniqueId());
        micActive.remove(player.getUniqueId());
    }
}

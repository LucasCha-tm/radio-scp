package fr.lucascha.radio.managers;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import fr.lucascha.radio.RadioPlugin;
import fr.lucascha.radio.frequencies.Frequency;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VoiceChatManager implements VoicechatPlugin {

    private static VoiceChatManager instance;
    private static VoicechatServerApi voicechatApi;
    private boolean available = false;
    private final Map<Frequency, Group> groups = new EnumMap<>(Frequency.class);

    /**
     * Joueurs dont le micro est BLOQUÉ côté serveur (talkie pas en main).
     * Leur audio sera ignoré par l'event MicrophonePacketEvent.
     */
    private final Set<UUID> blockedMic = new HashSet<>();

    public static VoiceChatManager getInstance() {
        if (instance == null) instance = new VoiceChatManager();
        return instance;
    }

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("voicechat") == null) {
            RadioPlugin.getInstance().getLogger().warning("SimpleVoiceChat non trouve - mode texte uniquement.");
            return;
        }
        BukkitVoicechatService service = Bukkit.getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(this);
            RadioPlugin.getInstance().getLogger().info("SimpleVoiceChat integre !");
        }
    }

    @Override
    public String getPluginId() { return "radio_plugin"; }

    @Override
    public void initialize(VoicechatApi api) {}

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(VoicechatServerStoppedEvent.class, this::onServerStopped);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatApi = event.getVoicechat();
        available = true;
        groups.clear();
        for (Frequency freq : Frequency.values()) {
            Group group = voicechatApi.groupBuilder()
                    .setName(freq.getColorCode() + freq.getIcon() + " " + freq.getDisplayName())
                    .setType(Group.Type.NORMAL)
                    .setPersistent(true)
                    .build();
            groups.put(freq, group);
        }
        RadioPlugin.getInstance().getLogger().info("Groupes vocaux radio crees !");
    }

    private void onServerStopped(VoicechatServerStoppedEvent event) {
        available = false;
        groups.clear();
        blockedMic.clear();
    }

    /**
     * Intercepte les paquets audio : si le joueur est dans blockedMic,
     * on annule le paquet → les autres membres du groupe n'entendent rien.
     */
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null) return;
        if (blockedMic.contains(connection.getPlayer().getUuid())) {
            event.cancel();
        }
    }

    /**
     * Rejoindre le groupe vocal avec micro actif (talkie en main).
     */
    public boolean joinFrequency(Player player, Frequency frequency) {
        if (!available || voicechatApi == null) return false;
        VoicechatConnection connection = voicechatApi.getConnectionOf(player.getUniqueId());
        if (connection == null) return false;
        Group group = groups.get(frequency);
        if (group == null) return false;
        connection.setGroup(group);
        blockedMic.remove(player.getUniqueId()); // micro actif
        return true;
    }

    /**
     * Reste dans le groupe mais bloque le micro (talkie retiré de la main).
     * Le joueur entend toujours les autres, mais ne peut pas parler.
     */
    public void blockMic(Player player) {
        blockedMic.add(player.getUniqueId());
    }

    /**
     * Réactive le micro (talkie repris en main).
     */
    public void unblockMic(Player player) {
        blockedMic.remove(player.getUniqueId());
    }

    /**
     * Quitter complètement le groupe vocal.
     */
    public void leaveGroup(Player player) {
        if (!available || voicechatApi == null) return;
        VoicechatConnection connection = voicechatApi.getConnectionOf(player.getUniqueId());
        if (connection == null) return;
        blockedMic.remove(player.getUniqueId());
        connection.setGroup(null);
    }

    public void onPlayerQuit(Player player) {
        leaveGroup(player);
    }

    public boolean isAvailable() { return available; }
}

package fr.lucascha.radio;

import fr.lucascha.radio.commands.RadioCommand;
import fr.lucascha.radio.listeners.CompassOverrideListener;
import fr.lucascha.radio.listeners.HandCheckListener;
import fr.lucascha.radio.listeners.PlayerQuitListener;
import fr.lucascha.radio.listeners.TalkieWalkieListener;
import fr.lucascha.radio.managers.RadioManager;
import fr.lucascha.radio.managers.VoiceChatManager;
import fr.lucascha.radio.utils.LuckPermsUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class RadioPlugin extends JavaPlugin {

    private static RadioPlugin instance;
    private HandCheckListener handCheckListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (LuckPermsUtil.init()) {
            getLogger().info("LuckPerms detecte.");
        }

        VoiceChatManager.getInstance().init();

        // HandCheckListener — vérifie que le talkie est en main
        handCheckListener = new HandCheckListener();
        RadioManager.getInstance().setHandCheckListener(handCheckListener);
        handCheckListener.startTask();

        getServer().getPluginManager().registerEvents(new CompassOverrideListener(), this);
        getServer().getPluginManager().registerEvents(new TalkieWalkieListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(handCheckListener, this);

        RadioCommand cmd = new RadioCommand(this);
        getCommand("radio").setExecutor(cmd);
        getCommand("radio").setTabCompleter(cmd);

        getLogger().info("RadioPlugin active !");
    }

    @Override
    public void onDisable() {
        if (handCheckListener != null) handCheckListener.stopTask();
        getLogger().info("RadioPlugin desactive.");
    }

    public static RadioPlugin getInstance() { return instance; }
}

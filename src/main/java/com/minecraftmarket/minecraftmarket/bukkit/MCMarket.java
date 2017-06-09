package com.minecraftmarket.minecraftmarket.bukkit;

import com.minecraftmarket.minecraftmarket.bukkit.Commands.MMCmd;
import com.minecraftmarket.minecraftmarket.bukkit.Configs.LayoutsConfig;
import com.minecraftmarket.minecraftmarket.bukkit.Configs.MainConfig;
import com.minecraftmarket.minecraftmarket.bukkit.Configs.SignsConfig;
import com.minecraftmarket.minecraftmarket.bukkit.Inventory.InventoryManager;
import com.minecraftmarket.minecraftmarket.bukkit.Listeners.ShopCmdListener;
import com.minecraftmarket.minecraftmarket.bukkit.Listeners.SignsListener;
import com.minecraftmarket.minecraftmarket.bukkit.Task.PurchasesTask;
import com.minecraftmarket.minecraftmarket.bukkit.Task.SignsTask;
import com.minecraftmarket.minecraftmarket.core.I18n;
import com.minecraftmarket.minecraftmarket.core.MCMApi;
import com.r4g3baby.pluginutils.Bukkit.Updater;
import com.r4g3baby.pluginutils.File.FileUtils;
import com.r4g3baby.pluginutils.Inventory.InventoryGUI;
import com.r4g3baby.pluginutils.Metrics.BukkitMetrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class MCMarket extends JavaPlugin {
    private I18n i18n;
    private MainConfig mainConfig;
    private LayoutsConfig layoutsConfig;
    private SignsConfig signsConfig;
    private MCMApi api;
    private boolean authenticated;
    private InventoryManager inventoryManager;
    private SignsTask signsTask;
    private PurchasesTask purchasesTask;

    @Override
    public void onEnable() {
        i18n = new I18n(getLanguageFolder(), getLogger());
        i18n.onEnable();

        mainConfig = new MainConfig(this);
        layoutsConfig = new LayoutsConfig(this);
        signsConfig = new SignsConfig(this);

        i18n.updateLocale(mainConfig.getLang());

        setKey(mainConfig.getApiKey(), false, null);

        if (mainConfig.isUseGUI()) {
            inventoryManager = new InventoryManager(this);
            getServer().getPluginManager().registerEvents(new ShopCmdListener(this), this);
            getServer().getPluginManager().registerEvents(InventoryGUI.getListener(), this);
        }

        if (mainConfig.isUseSigns()) {
            signsTask = new SignsTask(this);
            getServer().getScheduler().runTaskTimerAsynchronously(this, signsTask, 20 * 10, 20 * 60 * mainConfig.getCheckInterval());
            getServer().getPluginManager().registerEvents(new SignsListener(this), this);
        }
        getCommand("MinecraftMarket").setExecutor(new MMCmd(this));

        purchasesTask = new PurchasesTask(this);
        getServer().getScheduler().runTaskTimerAsynchronously(this, purchasesTask, 20 * 10, 20 * 60 * mainConfig.getCheckInterval());

        new BukkitMetrics(this);
        new Updater(this, 29183, pluginURL -> {
            getLogger().log(Level.WARNING, I18n.tl("newVersion"));
            getLogger().log(Level.WARNING, pluginURL);
        });
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        i18n.onDisable();
    }

    public void setKey(String apiKey, boolean save, Response<Boolean> response) {
        if (save) {
            mainConfig.setApiKey(apiKey);
        }
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            api = new MCMApi(apiKey, mainConfig.isDebug());
            authenticated = api.authAPI();
            if (!authenticated) {
                getLogger().log(Level.SEVERE, I18n.tl("invalidKey", "/MM apiKey <key>"));
            } else if (inventoryManager != null) {
                inventoryManager.load();
            }
            if (response != null) {
                response.done(authenticated);
            }
        });
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public LayoutsConfig getLayoutsConfig() {
        return layoutsConfig;
    }

    public SignsConfig getSignsConfig() {
        return signsConfig;
    }

    public MCMApi getApi() {
        return api;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public SignsTask getSignsTask() {
        return signsTask;
    }

    public PurchasesTask getPurchasesTask() {
        return purchasesTask;
    }

    public interface Response<T> {
        void done(T t);
    }

    private File getLanguageFolder() {
        File langFile = new File(getDataFolder(), "langs");
        if (!langFile.exists()) {
            for (String file : FileUtils.getJarResources(getClass().getProtectionDomain().getCodeSource())) {
                if (file.startsWith("langs/") && file.endsWith(".properties")) {
                    saveResource(file, true);
                }
            }
        }
        return langFile;
    }
}
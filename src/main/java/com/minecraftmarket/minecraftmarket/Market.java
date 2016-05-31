package com.minecraftmarket.minecraftmarket;

import com.minecraftmarket.minecraftmarket.command.CommandTask;
import com.minecraftmarket.minecraftmarket.gravitydevelopment.Updater;
import com.minecraftmarket.minecraftmarket.mcommands.Commands;
import com.minecraftmarket.minecraftmarket.recentgui.RecentListener;
import com.minecraftmarket.minecraftmarket.shop.ShopListener;
import com.minecraftmarket.minecraftmarket.shop.ShopTask;
import com.minecraftmarket.minecraftmarket.signs.SignListener;
import com.minecraftmarket.minecraftmarket.signs.SignUpdate;
import com.minecraftmarket.minecraftmarket.signs.Signs;
import com.minecraftmarket.minecraftmarket.util.Chat;
import com.minecraftmarket.minecraftmarket.util.Log;
import com.minecraftmarket.minecraftmarket.util.Settings;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.beans.ExceptionListener;
import java.io.IOException;

public class Market extends JavaPlugin {
	
    @Getter @Setter
	private Long interval;
    @Getter @Setter
	private String shopCommand;
	private boolean update;
    @Getter @Setter
	private boolean isBoardEnabled;
    @Getter @Setter
	private boolean isSignEnabled;
    @Getter @Setter
	private boolean isGuiEnabled;
    @Getter @Setter
	private static Market plugin;
    @Getter @Setter
	private CommandTask commandTask;
    @Getter @Setter
	private SignUpdate signUpdate;
    @Getter @Setter
    private String color;

	@Override
	public void onDisable() {
		stopTasks();
	}

	@Override
	public void onEnable() {
        plugin = this;
		try {
			ExceptionListener el = new ExceptionListener() {

				@Override
				public void exceptionThrown(Exception e) {
					Bukkit.broadcastMessage(e.getMessage());
				}

			};
			registerCommands();
			saveDefaultSettings();
			registerEvents();
			reload();
			startMetrics();
			startTasks();
		} catch (Exception e) {
			Log.log(e);
		}
	}

	public void reload() {
		try {
            Settings.get().reloadConfig();
            Settings.get().reloadLanguageConfig();
            
			loadConfigOptions();
            if (update)
                new Updater(this, 64572, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			if (authApi()) {
				startGUI();
				startSignTasks();
			}
		} catch (Exception e) {
			Log.log(e);
		}
	}

	private void loadConfigOptions() {
		Chat.get().SetupDefaultLanguage();
        FileConfiguration config = this.getConfig();
        Api.setApi(config.getString("ApiKey", "Apikey here"));
		this.interval = Math.max(config.getLong("Interval", 90L), 10L);
		this.isGuiEnabled = config.getBoolean("Enabled-GUI", true);
		this.shopCommand = config.getString("Shop-Command", "/shop");
		this.update = config.getBoolean("auto-update", true);
		this.isSignEnabled = config.getBoolean("Enabled-signs", true);
        this.color = config.getString("Color", "&0");
		Log.setDebugging(config.getBoolean("Debug", false));
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new ShopListener(), this);
		getServer().getPluginManager().registerEvents(new ShopCommand(), this);
		getServer().getPluginManager().registerEvents(new SignListener(), this);
		getServer().getPluginManager().registerEvents(new RecentListener(), this);
	}

	private boolean authApi() {
		if (Api.authAPI(Api.getKey())) {
			getLogger().info("Using API Key: " + Api.getKey());
			return true;
		} else {
			getLogger().warning("Invalid API Key! Use \"/MM APIKEY <APIKEY>\" to setup your APIKEY");
			return false;
		}
	}

	private void startMetrics() {
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			Log.log(e);
		}
	}

	private void startGUI() {
		if (isGuiEnabled) {
			new ShopTask().runTaskLater(this, 20L);
		}
	}

	private void runCommandChecker() {
		commandTask = new CommandTask();
		commandTask.runTaskTimerAsynchronously(this, 600L, interval * 20L);
	}

	private void startSignTasks() {
		if (isSignEnabled) {
			Signs.getSigns().setup();
			signUpdate = new SignUpdate();
			signUpdate.startSignTask();
		}
	}

	private void startTasks() {
		runCommandChecker();
	}

	private void registerCommands() {
		getCommand("mm").setExecutor(new Commands());
	}

	private void saveDefaultSettings() {
		Settings.get().LoadSettings();
	}

	private void stopTasks() {
		try {
			signUpdate.cancel();
			getServer().getScheduler().cancelTasks(this);
		} catch (Exception e) {
			Log.log(e);
		}
	}
    
}

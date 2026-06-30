package io.github.lifeflower;

import org.bukkit.plugin.java.JavaPlugin;

public class LifeFlowerPlugin extends JavaPlugin {
    private LifeFlowerStore store;
    private LifeFlowerManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.store = new LifeFlowerStore(this);
        this.manager = new LifeFlowerManager(this, store);
        this.manager.load();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, manager), this);
        getServer().getPluginManager().registerEvents(new BlockInteractionListener(this, manager), this);
        getServer().getPluginManager().registerEvents(new FlowerProtectionListener(manager), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, manager), this);

        getCommand("lifeflower").setExecutor(new LifeFlowerCommand(this, manager));

        getLogger().info("LifeFlower plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LifeFlower plugin disabled!");
    }
}

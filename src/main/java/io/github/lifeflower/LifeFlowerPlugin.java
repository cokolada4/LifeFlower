package io.github.lifeflower;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
        getServer().getPluginManager().registerEvents(new EntityInteractionListener(this, manager), this);
        getServer().getPluginManager().registerEvents(new FlowerProtectionListener(manager), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, manager), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                "lifeflower",
                "Main command for LifeFlower",
                List.of("lf"),
                new LifeFlowerCommand(this, manager)
            );
        });

        getLogger().info("LifeFlower plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LifeFlower plugin disabled!");
    }
}

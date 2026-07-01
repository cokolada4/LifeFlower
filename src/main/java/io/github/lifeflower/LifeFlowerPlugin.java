package io.github.lifeflower;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class LifeFlowerPlugin extends JavaPlugin {
    private LifeFlowerStore store;
    private LifeFlowerManager manager;
    private YamlConfiguration messages;

    public YamlConfiguration getMessages() {
        return messages;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessages();
        registerRecipe();
        this.store = new LifeFlowerStore(this);
        this.manager = new LifeFlowerManager(this, store);
        this.manager.load();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, manager), this);
        getServer().getPluginManager().registerEvents(new BlockInteractionListener(this, manager), this);
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

    private void loadMessages() {
        java.io.File file = new java.io.File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void registerRecipe() {
        if (!getConfig().getBoolean("recipe.enabled", true)) return;

        NamespacedKey key = new NamespacedKey(this, "raid_tool");
        getServer().removeRecipe(key);

        ItemStack result = LifeFlowerUtils.createRaidTool(this);
        result.setAmount(getConfig().getInt("recipe.output-amount", 1));

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        List<String> shape = getConfig().getStringList("recipe.shape");
        recipe.shape(shape.toArray(new String[0]));

        ConfigurationSection ingredients = getConfig().getConfigurationSection("recipe.ingredients");
        if (ingredients != null) {
            for (String charKey : ingredients.getKeys(false)) {
                String matName = ingredients.getString(charKey);
                if (matName != null) {
                    Material mat = Material.matchMaterial(matName);
                    if (mat != null) {
                        recipe.setIngredient(charKey.charAt(0), mat);
                    }
                }
            }
        }

        getServer().addRecipe(recipe);
    }
}

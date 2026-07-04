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

    public boolean isPluginEnabled() {
        return getConfig().getBoolean("enabled", true);
    }

    private LifeFlowerStore store;
    private LifeFlowerManager manager;
    private YamlConfiguration messages;
    private ParticleTask particleTask;

    public YamlConfiguration getMessages() {
        return messages;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessages();
        generateReferenceFile();

        this.store = new LifeFlowerStore(this);
        this.manager = new LifeFlowerManager(this, store);
        this.manager.load();

        startParticleTask();
        registerRecipe();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, manager), this);
        getServer().getPluginManager().registerEvents(new BlockInteractionListener(this, manager), this);
        getServer().getPluginManager().registerEvents(new ReviveListener(this, manager), this);
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
        if (particleTask != null) {
            particleTask.cancel();
        }
        getLogger().info("LifeFlower plugin disabled!");
    }

    public boolean reloadPlugin() {
        try {
            reloadConfig();
            loadMessages();
            startParticleTask();
            registerRecipe();
            return true;
        } catch (Exception e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Failed to reload plugin configuration", e);
            return false;
        }
    }

    private void loadMessages() {
        java.io.File file = new java.io.File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void registerRecipe() {
        registerRaidToolRecipe();
        registerReviveBeaconRecipe();
    }

    private void registerRaidToolRecipe() {
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

    private void registerReviveBeaconRecipe() {
        if (!getConfig().getBoolean("revive-beacon-recipe.enabled", true)) return;

        NamespacedKey key = new NamespacedKey(this, "revive_beacon");
        getServer().removeRecipe(key);

        ItemStack result = LifeFlowerUtils.createReviveBeacon(this);
        result.setAmount(getConfig().getInt("revive-beacon-recipe.output-amount", 1));

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        List<String> shape = getConfig().getStringList("revive-beacon-recipe.shape");
        recipe.shape(shape.toArray(new String[0]));

        ConfigurationSection ingredients = getConfig().getConfigurationSection("revive-beacon-recipe.ingredients");
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

    private void startParticleTask() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        particleTask = new ParticleTask(this, manager);
        long interval = getConfig().getLong("particles.interval", 10);
        particleTask.runTaskTimer(this, 20L, interval);
    }

    private void generateReferenceFile() {
        java.io.File file = new java.io.File(getDataFolder(), "materials_list.yml");
        if (file.exists()) return;

        YamlConfiguration ref = new YamlConfiguration();

        List<String> materials = java.util.Arrays.stream(Material.values())
                .map(Material::name)
                .sorted()
                .toList();
        ref.set("Materials", materials);

        List<String> enchantments = java.util.Arrays.stream(org.bukkit.enchantments.Enchantment.values())
                .map(e -> e.getKey().getKey().toUpperCase())
                .sorted()
                .toList();
        ref.set("Enchantments", enchantments);

        List<String> particles = java.util.Arrays.stream(org.bukkit.Particle.values())
                .map(org.bukkit.Particle::name)
                .sorted()
                .toList();
        ref.set("Particles", particles);

        try {
            ref.save(file);
        } catch (java.io.IOException e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Could not generate materials_list.yml", e);
        }
    }
}

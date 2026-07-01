package io.github.lifeflower;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LifeFlowerManagerTest {
    private LifeFlowerPlugin plugin;
    private LifeFlowerStore store;
    private LifeFlowerManager manager;

    @BeforeEach
    void setUp() {
        plugin = mock(LifeFlowerPlugin.class);
        FileConfiguration config = mock(FileConfiguration.class);
        when(plugin.getConfig()).thenReturn(config);
        when(config.getBoolean(anyString(), anyBoolean())).thenReturn(true);

        store = mock(LifeFlowerStore.class);
        // Spy to override isOccluding
        manager = spy(new LifeFlowerManager(plugin, store));
    }

    @Test
    void testCreateFlower() {
        UUID uuid = UUID.randomUUID();
        LifeFlower flower = manager.createFlower(uuid);

        assertNotNull(flower);
        assertEquals(uuid, flower.getOwnerUniqueId());
        assertFalse(flower.isPlanted());
        verify(store).save(anyMap());
    }

    @Test
    void testPlantFlower() {
        UUID uuid = UUID.randomUUID();
        manager.createFlower(uuid);

        Location loc = mock(Location.class);
        World world = mock(World.class);
        when(loc.getWorld()).thenReturn(world);
        when(world.getMaxHeight()).thenReturn(319);
        when(loc.getBlockY()).thenReturn(64);

        Block air = mock(Block.class);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(air);
        doReturn(false).when(manager).isOccluding(any());

        manager.plantFlower(uuid, loc, UUID.randomUUID());

        LifeFlower flower = manager.getFlower(uuid);
        assertTrue(flower.isPlanted());
        assertEquals(loc, flower.getLocation());
        assertTrue(flower.isValid());
    }

    @Test
    void testInvalidateSurface() {
        UUID uuid = UUID.randomUUID();
        manager.createFlower(uuid);

        Location loc = mock(Location.class);
        World world = mock(World.class);
        when(loc.getWorld()).thenReturn(world);
        when(world.getMaxHeight()).thenReturn(319);
        when(loc.getBlockY()).thenReturn(64);

        Block stone = mock(Block.class);
        when(world.getBlockAt(eq(0), eq(70), eq(0))).thenReturn(stone);

        Block air = mock(Block.class);
        when(world.getBlockAt(anyInt(), AdditionalMatchers.not(eq(70)), anyInt())).thenReturn(air);

        when(loc.getBlockX()).thenReturn(0);
        when(loc.getBlockZ()).thenReturn(0);

        doReturn(true).when(manager).isOccluding(eq(stone));
        doReturn(false).when(manager).isOccluding(AdditionalMatchers.not(eq(stone)));

        manager.plantFlower(uuid, loc, UUID.randomUUID());

        LifeFlower flower = manager.getFlower(uuid);
        assertFalse(flower.isValid());
    }
}

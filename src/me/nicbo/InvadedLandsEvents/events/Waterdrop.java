package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.Material;

public class Waterdrop extends InvadedEvent {

    private Material[][] mainCover;

    public Waterdrop(EventsMain plugin) {
        super("Waterdrop", "waterdrop", plugin);
    }

    @Override
    public void init(EventsMain plugin) {
        mainCover = new Material[5][5];
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    private void placeMainCover() {
        
    }

    public static class WaterdropPresets {
        private static Material water;
        private static Material redstone;
        public static Material[][] cover;

        static {
            water = Material.WATER;
            redstone = Material.REDSTONE_BLOCK;
            cover = new Material[][] {
                    { redstone, redstone, redstone, redstone, redstone },
                    { redstone, redstone, redstone, redstone, redstone },
                    { redstone, redstone, redstone, redstone, redstone },
                    { redstone, redstone, redstone, redstone, redstone },
                    { redstone, redstone, redstone, redstone, redstone }
            };
        }

        public Material[][] getRandomPreset() {
            return null;
        }

        public Material[][] getHoleCover() {
            Material[][] randomCover = cover.clone();
            return null;
        }
    }
}

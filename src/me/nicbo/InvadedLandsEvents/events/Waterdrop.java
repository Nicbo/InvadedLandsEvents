package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Material;

public class Waterdrop extends InvadedEvent {
    private Material[][] mainCover;

    public Material water;
    private Material redstone;

    private Material[][] openCover;
    private Material[][] closedCover;
    private Material[][] checkered;
    private Material[][] x;
    private Material[][] h;

    public Waterdrop(EventsMain plugin) {
        super("Waterdrop", "waterdrop", plugin);
        water = Material.WATER;
        redstone = Material.REDSTONE_BLOCK;

        openCover = new Material[][] {
                { water, water, water, water, water },
                { water, water, water, water, water },
                { water, water, water, water, water },
                { water, water, water, water, water },
                { water, water, water, water, water },
        };

        closedCover = new Material[][] {
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone }
        };

        x = new Material[][] {
                { water, redstone, redstone, redstone, water },
                { redstone, water, redstone, water, redstone },
                { redstone, redstone, water, redstone, redstone },
                { redstone, water, redstone, water, redstone },
                { water, redstone, redstone, redstone, water }
        };

        h = new Material[][] {
                { water, redstone, redstone, redstone, water },
                { water, redstone, redstone, redstone, water },
                { water, water, water, water, water },
                { water, redstone, redstone, redstone, water },
                { water, redstone, redstone, redstone, water }
        };

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
}

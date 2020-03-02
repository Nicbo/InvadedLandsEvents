package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Material;

public class Waterdrop extends InvadedEvent {
    private Material[][] mainCover;

    private Material water;
    private Material redstone;

    private Material[][] openCover;
    private Material[][] closedCover;
    private Material[][] cross;
    private Material[][] x;

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

        cross = new Material[][] {
                { redstone, redstone, water, redstone, redstone },
                { redstone, redstone, water, redstone, redstone },
                { water, water, water, water, water },
                { redstone, redstone, water, redstone, redstone },
                { redstone, redstone, water, redstone, redstone }
        };

        x = new Material[][] {
                { water, redstone, redstone, redstone, water },
                { redstone, water, redstone, water, redstone },
                { redstone, redstone, water, redstone, redstone },
                { redstone, water, redstone, water, redstone },
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

    // Depending on round return line / hole / cross / open
    public void changeCover(int round) {

    }

    public void setLineCover(int lines) {
        boolean vertical = GeneralUtils.randomBoolean();
        boolean side = GeneralUtils.randomBoolean();

        mainCover = openCover.clone();
        for (int i = 0; i < lines; i++) {
            for (int j = side ? 0 : 5; i < 5; i += side ? 1 : -1) {
                if (vertical) {
                    mainCover[j][i] = water;
                } else {
                    mainCover[i][j] = water;
                }
            }
        }
    }

    public void setHoleCover(int holes) {
        mainCover = closedCover.clone();
        for (int i = 0; i < holes; i++) {
            int row = GeneralUtils.randomMinMax(0, 4);
            int column = GeneralUtils.randomMinMax(0, 4);
            mainCover[row][column] = water;
        }
    }
}

package ca.nicbo.invadedlandsevents.task.world;

import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Replaces all blocks in a {@link CuboidRegion} with a provided {@link Material}.
 *
 * @author Nicbo
 */
public class BlockPlacementTask extends SyncedTask {
    private static final long DELAY = 0;
    private static final long PERIOD = 1;

    private static final int MAX_MS_PER_TICK = 25;

    private final Queue<Block> blockQueue;
    private final Material material;

    public BlockPlacementTask(CuboidRegion region, Material material) {
        super(DELAY, PERIOD);
        Validate.checkArgumentNotNull(region, "region");
        Validate.checkArgumentNotNull(material, "material");
        this.blockQueue = new ArrayDeque<>(region.getBlocks());
        this.material = material;
    }

    @Override
    protected void run() {
        final long stopTime = System.currentTimeMillis() + MAX_MS_PER_TICK;
        while (System.currentTimeMillis() <= stopTime) {
            if (blockQueue.isEmpty()) {
                stop();
                break;
            }

            Block block = blockQueue.remove();
            if (block.getType() != material) {
                block.setType(material);
            }
        }
    }
}

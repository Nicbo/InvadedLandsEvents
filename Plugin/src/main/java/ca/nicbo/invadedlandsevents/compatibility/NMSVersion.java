package ca.nicbo.invadedlandsevents.compatibility;

import ca.nicbo.invadedlandsevents.exception.UnsupportedVersionException;
import org.bukkit.Bukkit;

/**
 * The different supported NMS versions.
 *
 * @author Nicbo
 */
public enum NMSVersion {
    v1_8_R1, v1_8_R2, v1_8_R3,
    v1_9_R1, v1_9_R2,
    v1_10_R1,
    v1_11_R1,
    v1_12_R1,
    v1_13_R1, v1_13_R2,
    v1_14_R1,
    v1_15_R1,
    v1_16_R1, v1_16_R2, v1_16_R3,
    v1_17_R1;

    private static final NMSVersion CURRENT_VERSION;

    static {
        String packageVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            CURRENT_VERSION = NMSVersion.valueOf(packageVersion);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedVersionException("unsupported NMS version: " + packageVersion, e);
        }
    }

    public boolean isPreCombatUpdate() {
        return compareTo(v1_9_R1) < 0;
    }

    public boolean isLegacy() {
        return compareTo(v1_13_R1) < 0;
    }

    public static NMSVersion getCurrentVersion() {
        return CURRENT_VERSION;
    }
}

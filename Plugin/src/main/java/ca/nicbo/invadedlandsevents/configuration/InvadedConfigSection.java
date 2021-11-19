package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.api.configuration.ConfigSection;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigValueType;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link ConfigSection}.
 *
 * @author Nicbo
 */
public class InvadedConfigSection implements ConfigSection {
    private static final String TYPE_SUFFIX = ".type";
    private static final String DESCRIPTION_SUFFIX = ".description";
    private static final String VALUE_SUFFIX = ".value";

    private final String name;
    private final InvadedConfigHandler configHandler;

    private final ConfigurationSection configurationSection;

    public InvadedConfigSection(String name, InvadedConfigHandler configHandler) {
        Validate.checkArgumentNotNull(name, "name");
        Validate.checkArgumentNotNull(configHandler, "configHandler");
        this.name = name;
        this.configHandler = configHandler;
        this.configurationSection = configHandler.getInternalConfiguration().getConfigurationSection(name);
        Validate.checkNotNull(configurationSection, "config.yml does not contain %s", name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean getBoolean(String key) {
        checkContainsKeyWithCorrectType(key, ConfigValueType.BOOLEAN);
        return configurationSection.getBoolean(key + VALUE_SUFFIX);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        setValue(key, ConfigValueType.BOOLEAN, value);
    }

    @Override
    public int getInteger(String key) {
        checkContainsKeyWithCorrectType(key, ConfigValueType.INTEGER);
        return configurationSection.getInt(key + VALUE_SUFFIX);
    }

    @Override
    public void setInteger(String key, int value) {
        setValue(key, ConfigValueType.INTEGER, value);
    }

    @Override
    public Kit getKit(String key) {
        checkContainsKeyWithCorrectType(key, ConfigValueType.KIT);
        return ConfigSerialization.deserializeKit(configurationSection.getString(key + VALUE_SUFFIX));
    }

    @Override
    public void setKit(String key, Kit value) {
        setValue(key, ConfigValueType.KIT, ConfigSerialization.serializeKit(value));
    }

    @Override
    public Location getLocation(String key) {
        checkContainsKeyWithCorrectType(key, ConfigValueType.LOCATION);
        return ConfigSerialization.deserializeLocation(configurationSection.getString(key + VALUE_SUFFIX));
    }

    @Override
    public void setLocation(String key, Location value) {
        setValue(key, ConfigValueType.LOCATION, ConfigSerialization.serializeLocation(value));
    }

    @Override
    public CuboidRegion getRegion(String key) {
        checkContainsKeyWithCorrectType(key, ConfigValueType.REGION);
        return ConfigSerialization.deserializeRegion(configurationSection.getString(key + VALUE_SUFFIX));
    }

    @Override
    public void setRegion(String key, CuboidRegion value) {
        setValue(key, ConfigValueType.REGION, ConfigSerialization.serializeRegion(value));
    }

    @Override
    public List<String> getStringList(String key) {
        checkContainsKeyWithCorrectType(key, ConfigValueType.STRING_LIST);
        return configurationSection.getStringList(key + VALUE_SUFFIX);
    }

    @Override
    public void setStringList(String key, List<String> value) {
        setValue(key, ConfigValueType.STRING_LIST, value);
    }

    @Override
    public ConfigValueType getType(String key) {
        checkContainsKey(key);
        return ConfigValueType.valueOf(configurationSection.getString(key + TYPE_SUFFIX));
    }

    @Override
    public String getDescription(String key) {
        checkContainsKey(key);
        // noinspection ConstantConditions (we check if the key exists beforehand, this will not return null)
        return configurationSection.getString(key + DESCRIPTION_SUFFIX);
    }

    @Override
    public Set<String> getKeys() {
        return configurationSection.getKeys(false);
    }

    private void setValue(String key, ConfigValueType type, Object value) {
        checkContainsKeyWithCorrectType(key, type);
        Validate.checkArgumentNotNull(value, "value");
        configurationSection.set(key + VALUE_SUFFIX, value);
        configHandler.save();
    }

    // Checks if section contains key
    private void checkContainsKey(String key) {
        Validate.checkArgumentNotNull(key, "key");
        Validate.checkArgument(configurationSection.contains(key), "%s does not contain %s", key, key);
    }

    // Checks if section contains key and the type is correct
    private void checkContainsKeyWithCorrectType(String key, ConfigValueType type) {
        ConfigValueType configType = getType(key); // calls checkContainsKey
        Validate.checkArgumentNotNull(type, "type");
        Validate.checkArgument(type == configType, "config value type for %s is %s, not %s", key, configType, type);
    }
}

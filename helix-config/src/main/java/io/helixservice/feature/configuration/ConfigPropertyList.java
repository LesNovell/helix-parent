
package io.helixservice.feature.configuration;

import io.helixservice.feature.configuration.provider.ConfigProvider;
import io.helixservice.feature.configuration.provider.ConfigProviderPropertiesChangedListener;
import io.helixservice.feature.configuration.provider.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a configuration property which is a list of items.
 * <p>
 * The property returned will be from the current environment's
 * configuration or the default configuration.
 */
public class ConfigPropertyList implements ConfigProviderPropertiesChangedListener {
    private ConfigPropertyListChangedListener configPropertyListChangedListener;

    private ConfigProvider configProvider;
    private String propertyName;
    private List<Property> properties = new ArrayList<>();

    public ConfigPropertyList(ConfigProvider configProvider, String propertyName) {
        this.configProvider = configProvider;
        this.propertyName = propertyName;
        rebuildList();
    }

    /**
     * The size of the list of values in this property
     * @return Size of the list
     */
    public int size() {
        return properties.size();
    }

    /**
     * Get the value at the given index
     *
     * @param index Index into the property list
     * @return The value
     */
    public String valueAt(int index) {
        return properties.get(index).getValue();
    }

    /**
     * Get the entire property value list
     *
     * @return A list of property values
     */
    public List<String> toList() {
        return properties.stream().map(Property::getValue).collect(Collectors.toList());
    }

    /**
     * Set a change listener for this property. An initial property value will be sent.
     * @param configPropertyChangedListener Change listener to set on this property
     */
    public void setChangeListener(ConfigPropertyListChangedListener configPropertyChangedListener) {
        setChangeListener(true, configPropertyChangedListener);
    }

    /**
     * Set a change listener for this property
     * <p>
     * @param fireInitial If true, the change listener will be called with the initial property value
     * @param configPropertyChangedListener Change listener to set on this property
     */
    public void setChangeListener(boolean fireInitial, ConfigPropertyListChangedListener configPropertyChangedListener) {
        this.configPropertyListChangedListener = configPropertyChangedListener;

        if (fireInitial) {
            configPropertyChangedListener.propertyChanged(this);
        }
    }

    /**
     * Remove the property change listener
     */
    public void clearPropertiesChangeHandler() {
        this.configPropertyListChangedListener = null;
    }

    @Override
    public void configChanged(List<String> newProperties, List<String> changedProperties, List<String> deletedProperties) {
        Set<String> modifiedProperties = new HashSet<>();
        modifiedProperties.addAll(newProperties);
        modifiedProperties.addAll(changedProperties);
        modifiedProperties.addAll(deletedProperties);

        for (String modifiedProperty : modifiedProperties) {
            if (modifiedProperty.startsWith(propertyName + "[")) {
                rebuildList();
                configPropertyListChangedListener.propertyChanged(this);
                break;
            }
        }
    }


    private void rebuildList() {
        properties.clear();
        Property nextProperty;
        for(int index=0; null != (nextProperty = configProvider.propertyByName(propertyName + "[" + index + "]")); index++) {
            properties.add(nextProperty);
        }
        configProvider.addPropertiesChangedListener(this);
    }
}

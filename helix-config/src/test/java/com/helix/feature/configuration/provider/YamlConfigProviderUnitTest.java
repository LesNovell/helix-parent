/*
 * @author Les Novell
 *
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 */

package com.helix.feature.configuration.provider;

import com.helix.feature.configuration.locator.ClasspathResourceLocator;
import com.helix.feature.configuration.locator.ResourceLocator;
import com.helix.feature.configuration.resolver.PropertyResolver;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.ByteArrayInputStream;
import java.util.Observable;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YamlConfigProviderUnitTest {

    private Observable updatedProperty;

    @Test(expected = ConfigProviderException.class)
    public void testFailToFindBaseYaml() throws Exception {
        ResourceLocator resourceLocator = mock(ClasspathResourceLocator.class);
        when(resourceLocator.getStream(Matchers.anyString())).thenReturn(Optional.empty());

        new ConfigProvider(new String[] {"default"}, "my.yaml",
                0, () -> new ResourceLocator[] {resourceLocator},
                () -> new PropertyResolver[] {});
    }

    @Test
    public void testLoadsBaseYaml() throws Exception {
        String yamlInput = "nothing: here";

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(yamlInput.getBytes());
        ResourceLocator resourceLocator = mock(ClasspathResourceLocator.class);
        when(resourceLocator.getStream(Matchers.anyString())).thenReturn(Optional.of(byteArrayInputStream));

        new ConfigProvider(new String[] {"default"}, "my.yaml",
                0, () -> new ResourceLocator[] {resourceLocator},
                () -> new PropertyResolver[] {});
    }

    @Test
    public void testGetBaseYamlProperty() throws Exception {
        String yamlInput = "yaml.property: Hello";

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(yamlInput.getBytes());
        ResourceLocator resourceLocator = mock(ClasspathResourceLocator.class);
        when(resourceLocator.getStream(Matchers.anyString())).thenReturn(Optional.of(byteArrayInputStream));

        ConfigProvider provider =
                new ConfigProvider(new String[] {"default"}, "my.yaml",
                        0, () -> new ResourceLocator[] {resourceLocator},
                        () -> new PropertyResolver[] {});

        Property property = provider.propertyByName("yaml.property");
        assertEquals("Hello", property.getValue());
    }

    @Test
    public void testReloadProperties() throws Exception {
        String yamlInput = "yaml.property: Hello";

        ResourceLocator resourceLocator = mock(ClasspathResourceLocator.class);
        when(resourceLocator.getStream(Matchers.anyString())).thenAnswer(
                a -> Optional.of(new ByteArrayInputStream(yamlInput.getBytes())));

        ConfigProvider provider =
                new ConfigProvider(new String[] {"default"}, "my.yaml",
                        0, () -> new ResourceLocator[] {resourceLocator},
                        () -> new PropertyResolver[] {});

        Property property = provider.propertyByName("yaml.property");
        assertEquals("Hello", property.getValue());

        // Test reload does not mess up the existing property
        provider.reloadProperties();
        assertEquals("Hello", property.getValue());
    }

    @Test
    public void testReloadPropertiesNewValue() throws Exception {
        ResourceLocator resourceLocator = mock(ClasspathResourceLocator.class);
        when(resourceLocator.getStream(Matchers.anyString())).thenAnswer(
                a -> Optional.of(new ByteArrayInputStream("yaml.property: Hello".getBytes())));

        ConfigProvider provider =
                new ConfigProvider(new String[] {"default"}, "my.yaml",
                        0, () -> new ResourceLocator[] {resourceLocator},
                        () -> new PropertyResolver[] {});

        Property property = provider.propertyByName("yaml.property");
        assertEquals("Hello", property.getValue());

        // Test reload does not mess up the existing property
        when(resourceLocator.getStream(Matchers.anyString())).thenAnswer(
                a -> Optional.of(new ByteArrayInputStream("yaml.property: Goodbye".getBytes())));

        provider.reloadProperties();
        assertEquals("Goodbye", property.getValue());
    }

    @Test
    public void testPropertyChangeNotification() throws Exception {
        ResourceLocator resourceLocator = mock(ClasspathResourceLocator.class);
        when(resourceLocator.getStream(Matchers.anyString())).thenAnswer(
                a -> Optional.of(new ByteArrayInputStream("yaml.property: Hello".getBytes())));

        ConfigProvider provider =
                new ConfigProvider(new String[] {"default"}, "my.yaml",
                        0, () -> new ResourceLocator[] {resourceLocator},
                        () -> new PropertyResolver[] {});

        Property property = provider.propertyByName("yaml.property");
        assertEquals("Hello", property.getValue());
        property.addObserver((o, arg) -> updatedProperty = o);

        // Test reload does not mess up the existing property
        when(resourceLocator.getStream(Matchers.anyString())).thenAnswer(
                a -> Optional.of(new ByteArrayInputStream("yaml.property: Goodbye".getBytes())));

        provider.reloadProperties();
        assertEquals("Goodbye", property.getValue());
        assertEquals(updatedProperty, property);
    }

    @Test
    public void testGetYamlPropertyFrom2ndProfilePath() throws Exception {
        ResourceLocator resourceLocator = mock(ClasspathResourceLocator.class);
        when(resourceLocator.getStream("default/my.yaml"))
                .thenReturn(Optional.of(
                        new ByteArrayInputStream((
                                "yaml.property: Hello\n"
                                        + "yaml.original: Unmodified\n").getBytes())));

        when(resourceLocator.getStream("dev/my.yaml"))
                .thenReturn(Optional.of(
                        new ByteArrayInputStream(
                                ("yaml.property: Overlaid\n"
                                        + "yaml.new: NewProperty").getBytes())));

        ConfigProvider subject =
                new ConfigProvider(new String[] {"default","dev"}, "my.yaml",
                        0, () -> new ResourceLocator[] {resourceLocator},
                        () -> new PropertyResolver[] {});

        Property overlayProperty = subject.propertyByName("yaml.property");
        assertEquals("Overlaid", overlayProperty.getValue());

        Property originalProperty = subject.propertyByName("yaml.original");
        assertEquals("Unmodified", originalProperty.getValue());

        Property newProperty = subject.propertyByName("yaml.new");
        assertEquals("NewProperty", newProperty.getValue());

    }
}

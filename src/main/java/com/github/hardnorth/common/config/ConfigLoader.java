/*
 * Copyright 2020 Vadzim Hushchanskou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hardnorth.common.config;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import org.cfg4j.source.inmemory.InMemoryConfigurationSource;
import org.cfg4j.source.system.SystemPropertiesConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * A config loading class from various sources. Reads property files, environment and system variables.
 * Each layer overrides previous values. The order is (from least to the most important):
 * property file &lt;- environment variables &lt;- system properties
 */
public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    /**
     * Common system property name to pass environment name.
     */
    public static final String ENVIRONMENT_PROPERTY = "env";
    public static final String DEFAULT_ENVIRONMENT_NAME = "default";

    private final ConfigurationSource defaultSource;
    private final ClassLoader classLoader;

    /**
     * Constructor the Provider without default property values.
     */
    public ConfigLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Constructs the Provider using default {@link Properties}.
     *
     * @param defaultProperties default property values
     */
    public ConfigLoader(Properties defaultProperties) {
        this(defaultProperties, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Constructs the Provider using specific {@link ClassLoader}.
     *
     * @param contextClassLoader a class loader which will be used to locate properties
     */
    public ConfigLoader(ClassLoader contextClassLoader) {
        defaultSource = null;
        classLoader = contextClassLoader;
    }

    /**
     * Constructs the Provider using default {@link Properties} and specific {@link ClassLoader}.
     *
     * @param defaultProperties  default property values
     * @param contextClassLoader a class loader which will be used to locate properties
     */
    public ConfigLoader(Properties defaultProperties, ClassLoader contextClassLoader) {
        defaultSource = new InMemoryConfigurationSource(defaultProperties);
        classLoader = contextClassLoader;
    }

    private static String getEnvironmentFile(ConfigurationSource[] sources) {
        ConfigurationProvider config = new ConfigurationProviderBuilder()
                .withConfigurationSource(new MergeConfigurationSource(sources)).build();
        try {
            return config.getProperty(ENVIRONMENT_PROPERTY, String.class);
        } catch (NoSuchElementException e) {
            return DEFAULT_ENVIRONMENT_NAME;
        }
    }

    /**
     * Returns a {@link ConfigProvider} class, a property value getter.
     *
     * @return property provider
     */
    public ConfigProvider get() {
        List<ConfigurationSource> sources = new ArrayList<>();

        // Default property values are set here
        if (defaultSource != null) {
            sources.add(defaultSource);
        }

        // System environment variables
        Properties environmentProperties = new Properties();
        environmentProperties.putAll(System.getenv());
        sources.add(new InMemoryConfigurationSource(environmentProperties));

        // System property variables (-Dproperty=value)
        sources.add(new SystemPropertiesConfigurationSource());

        // locate propertyFile inside class-loader resources
        String propertyFile = getEnvironmentFile(sources.toArray(new ConfigurationSource[0]));
        if (propertyFile != null) {
            String propertyFilePath = propertyFile + ".properties";
            InputStream propertyResource = classLoader.getResourceAsStream(propertyFilePath);
            if (propertyResource != null) {
                Properties properties = new Properties();
                try {
                    properties.load(propertyResource);
                } catch (IOException e) {
                    throw new IllegalStateException(String.format("Unable to load property file '%s': %s", propertyFilePath,
                            e.getMessage()), e);
                }
                sources.add(1, new InMemoryConfigurationSource(properties));
            } else {
                LOGGER.warn(String.format("Unable to find property file '%s' inside classpath.", propertyFilePath));
            }
        }

        // Each property source overrides previously defined values (if any)
        ConfigurationSource source = new MergeConfigurationSource(sources.toArray(new ConfigurationSource[0]));

        // Resolve placeholders inside properties
        source = new PlaceholderConfigSource(source);
        final ConfigurationProvider result = new ConfigurationProviderBuilder().withConfigurationSource(source).build();
        return new InternalConfigProvider(result);
    }


    private static class InternalConfigProvider implements ConfigProvider {
        private final ConfigurationProvider config;

        private InternalConfigProvider(final ConfigurationProvider provider) {
            this.config = provider;
        }

        @Override
        public <T> T getProperty(String key, Class<T> type) {
            return config.getProperty(key, type);
        }

        @Override
        public <T> T getProperty(String key, Class<T> type, T defaultValue) {
            try {
                return config.getProperty(key, type);
            } catch (NoSuchElementException e) {
                return defaultValue;
            }
        }
    }
}

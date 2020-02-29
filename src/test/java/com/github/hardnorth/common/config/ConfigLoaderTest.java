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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;

public class ConfigLoaderTest {

    private static final String PROPERTY_PREFIX = "com.github.hardnorth.common.config.test.";

    @Test
    public void test_default_file_load() {
        ConfigLoader loader = new ConfigLoader();
        String propertyValue = loader.get().getProperty(PROPERTY_PREFIX + "file", String.class);
        assertThat(propertyValue, equalTo(ConfigLoader.DEFAULT_ENVIRONMENT_NAME + ".properties"));
    }

    @Test
    public void test_not_default_file_load() {
        System.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "env");
        try {
            ConfigLoader loader = new ConfigLoader();
            String propertyValue = loader.get().getProperty(PROPERTY_PREFIX + "file", String.class);
            assertThat(propertyValue, equalTo("env.properties"));
        } finally {
            System.clearProperty(ConfigLoader.ENVIRONMENT_PROPERTY);
        }
    }

    @Test
    public void test_different_types_of_property_load() {
        System.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "different_types");
        try {
            ConfigLoader loader = new ConfigLoader();
            String stringValue = loader.get().getProperty(PROPERTY_PREFIX + "string", String.class);
            assertThat(stringValue, equalTo("my string property"));

            Boolean booleanValue = loader.get().getProperty(PROPERTY_PREFIX + "boolean", Boolean.class);
            assertThat(booleanValue, equalTo(Boolean.TRUE));

            Byte byteValue = loader.get().getProperty(PROPERTY_PREFIX + "byte", Byte.class);
            assertThat(byteValue, equalTo((byte) 1));

            Integer intValue = loader.get().getProperty(PROPERTY_PREFIX + "int", Integer.class);
            assertThat(intValue, equalTo(1000));

            Long longValue = loader.get().getProperty(PROPERTY_PREFIX + "long", Long.class);
            assertThat(longValue, equalTo(100000000000L));

            Character charValue = loader.get().getProperty(PROPERTY_PREFIX + "char", Character.class);
            assertThat(charValue, equalTo('c'));
        } finally {
            System.clearProperty(ConfigLoader.ENVIRONMENT_PROPERTY);
        }
    }

    @Test
    public void test_placeholder_load_error() {
        System.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "placeholder_error");
        try {
            ConfigLoader loader = new ConfigLoader();
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> loader.get().getProperty(PROPERTY_PREFIX + "placeholder.recursive.not.resolved", String.class));
        } finally {
            System.clearProperty(ConfigLoader.ENVIRONMENT_PROPERTY);
        }
    }

    @Test
    public void test_no_placeholder_load() {
        System.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "placeholder");
        try {
            ConfigLoader loader = new ConfigLoader();
            Assertions.assertThrows(NoSuchElementException.class,
                    () -> loader.get().getProperty(PROPERTY_PREFIX + "placeholder.not.resolved", String.class));
        } finally {
            System.clearProperty(ConfigLoader.ENVIRONMENT_PROPERTY);
        }
    }

    @Test
    public void test_placeholder_load() {
        System.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "placeholder");
        try {
            ConfigLoader loader = new ConfigLoader();
            Boolean booleanValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.boolean.value", Boolean.class);
            assertThat(booleanValue, equalTo(Boolean.TRUE));

            String stringValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.string.value", String.class);
            assertThat(stringValue, equalTo("my string property"));

            Byte byteValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.recursive.resolve", Byte.class);
            assertThat(byteValue, equalTo((byte) 2));

            stringValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.part.value.string", String.class);
            assertThat(stringValue, equalTo("this is my string property"));

            Integer intValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.part.value.int", Integer.class);
            assertThat(intValue, equalTo(10002));

            stringValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.empty.value", String.class);
            assertThat(stringValue, emptyString());
        } finally {
            System.clearProperty(ConfigLoader.ENVIRONMENT_PROPERTY);
        }
    }

    @Test
    public void test_placeholder_default_value_load() {
        System.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "placeholder_default");
        try {
            ConfigLoader loader = new ConfigLoader();
            String stringValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.default.string", String.class);
            assertThat(stringValue, equalTo("my default string property"));

            stringValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.default.empty.string", String.class);
            assertThat(stringValue, emptyString());

            Byte byteValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.recursive.not.resolved.default", Byte.class);
            assertThat(byteValue, equalTo((byte) 3));

            stringValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.default.string.colons", String.class);
            assertThat(stringValue, equalTo("my:default:string:property"));

            Integer intValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.default.int", Integer.class);
            assertThat(intValue, equalTo(10003));

            byteValue = loader.get().getProperty(PROPERTY_PREFIX + "placeholder.default.recursive", Byte.class);
            assertThat(byteValue, equalTo((byte) 3));
        } finally {
            System.clearProperty(ConfigLoader.ENVIRONMENT_PROPERTY);
        }
    }
}

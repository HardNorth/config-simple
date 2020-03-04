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
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConfigLoaderTest {

    private static final String PROPERTY_PREFIX = "com.github.hardnorth.common.config.test.";

    @Test
    public void test_default_value_provide() {
        ConfigProvider provider = new ConfigLoader(getClass().getClassLoader()).get();
        String stringValue = provider.getProperty(PROPERTY_PREFIX + "not.existing.value", String.class, "my not existing default value");
        assertThat(stringValue, equalTo("my not existing default value"));
    }

    @Test
    public void test_default_file_load() {
        ConfigProvider provider = new ConfigLoader(getClass().getClassLoader()).get();
        String propertyValue = provider.getProperty(PROPERTY_PREFIX + "file", String.class);
        assertThat(propertyValue, equalTo(ConfigLoader.DEFAULT_ENVIRONMENT_NAME + ".properties"));
    }

    @Test
    public void test_not_default_file_load() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "env");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();
        String propertyValue = provider.getProperty(PROPERTY_PREFIX + "file", String.class);
        assertThat(propertyValue, equalTo("env.properties"));
    }

    @Test
    public void test_different_types_of_property_load() throws MalformedURLException {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "different_types");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();
        String stringValue = provider.getProperty(PROPERTY_PREFIX + "string", String.class);
        assertThat(stringValue, equalTo("my string property"));

        Boolean booleanValue = provider.getProperty(PROPERTY_PREFIX + "boolean", Boolean.class);
        assertThat(booleanValue, equalTo(Boolean.TRUE));

        Byte byteValue = provider.getProperty(PROPERTY_PREFIX + "byte", Byte.class);
        assertThat(byteValue, equalTo((byte) 1));

        Integer intValue = provider.getProperty(PROPERTY_PREFIX + "int", Integer.class);
        assertThat(intValue, equalTo(1000));

        Long longValue = provider.getProperty(PROPERTY_PREFIX + "long", Long.class);
        assertThat(longValue, equalTo(100000000000L));

        Character charValue = provider.getProperty(PROPERTY_PREFIX + "char", Character.class);
        assertThat(charValue, equalTo('c'));

        URL urlConversion = provider.getProperty(PROPERTY_PREFIX + "url", URL.class);
        assertThat(urlConversion, equalTo(new URL("https://www.example.com")));
    }

    @Test
    public void test_placeholder_load_error() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "placeholder_error");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();

        String stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.recursive.not.resolved", String.class);
        assertThat(stringValue, equalTo("${${THERE_IS_NO_SUCH_PLACEHOLDER}_PLACEHOLDER}"));
    }

    @Test
    public void test_no_placeholder_load() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "placeholder");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();
        String stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.not.resolved", String.class);
        assertThat(stringValue, equalTo("${THERE_IS_NO_SUCH_PLACEHOLDER}"));
    }

    @Test
    public void test_placeholder_load() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "placeholder");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();
        Boolean booleanValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.boolean.value", Boolean.class);
        assertThat(booleanValue, equalTo(Boolean.TRUE));

        String stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.string.value", String.class);
        assertThat(stringValue, equalTo("my string property"));

        Byte byteValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.recursive.resolve", Byte.class);
        assertThat(byteValue, equalTo((byte) 2));

        stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.part.value.string", String.class);
        assertThat(stringValue, equalTo("this is my string property"));

        Integer intValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.part.value.int", Integer.class);
        assertThat(intValue, equalTo(10002));

        stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.empty.value", String.class);
        assertThat(stringValue, emptyString());

        stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.two.values", String.class);
        assertThat(stringValue, equalTo("SECOND my string property"));
    }

    @Test
    public void test_placeholder_default_value_load() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "placeholder_default");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();
        String stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.default.string", String.class);
        assertThat(stringValue, equalTo("my default string property"));

        stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.default.empty.string", String.class);
        assertThat(stringValue, emptyString());

        Byte byteValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.recursive.not.resolved.default", Byte.class);
        assertThat(byteValue, equalTo((byte) 3));

        stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.default.string.colons", String.class);
        assertThat(stringValue, equalTo("my:default:string:property"));

        Integer intValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.default.int", Integer.class);
        assertThat(intValue, equalTo(10003));

        byteValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.default.recursive", Byte.class);
        assertThat(byteValue, equalTo((byte) 3));
    }

    @Test
    public void test_placeholder_special_cases_load() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "specific_placeholders");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();

        String stringValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.debian", String.class);
        assertThat(stringValue, equalTo("+($debian_chroot)\\u@\\h:\\w\\$"));
    }

    @Test
    public void test_placeholder_infinite_recursive() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "infinite_recursive_placeholder");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();

        IllegalStateException exc = Assertions.assertThrows(IllegalStateException.class,
                () -> provider.getProperty(PROPERTY_PREFIX + "placeholder.recursive.one", String.class));
        assertThat(exc.getCause().getClass(), equalTo(IllegalStateException.class));
        assertThat(exc.getCause().getMessage(),
                equalTo("Infinite loop in property interpolation of ${com.github.hardnorth.common.config.test.placeholder.recursive.one}: com.github.hardnorth.common.config.test.placeholder.recursive.one->com.github.hardnorth.common.config.test.placeholder.recursive.two"));
    }

    @Test
    public void test_placeholder_infinite_recursive_10() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "10_infinite_recursive_placeholder");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();

        IllegalStateException exc = Assertions.assertThrows(IllegalStateException.class,
                () -> provider.getProperty(PROPERTY_PREFIX + "placeholder.depth.one", String.class));
        assertThat(exc.getCause().getClass(), equalTo(IllegalStateException.class));
        assertThat(exc.getCause().getMessage(),
                startsWith("Infinite loop in property interpolation of ${com.github.hardnorth.common.config.test.placeholder.depth.nine}"));
    }

    @Test
    public void test_maximum_depth_placeholder() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "maximum_depth_placeholder");
        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();

        Integer intValue = provider.getProperty(PROPERTY_PREFIX + "placeholder.depth.one", Integer.class);
        assertThat(intValue, equalTo(11));
    }

    @Test
    public void test_default_property_get_when_there_is_no_overrides() throws IOException {
        Properties props = new Properties();
        props.load(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("value_override/default.properties")));
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "value_override/file");

        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();

        String stringValue = provider.getProperty(PROPERTY_PREFIX + "default.value", String.class);
        assertThat(stringValue, equalTo("my default value"));
    }

    @Test
    public void test_default_property_override_with_an_environment_file() throws IOException {
        Properties props = new Properties();
        props.load(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("value_override/default_file.properties")));
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "value_override/file");

        ConfigProvider provider = new ConfigLoader(props, getClass().getClassLoader()).get();

        String stringValue = provider.getProperty(PROPERTY_PREFIX + "file.value", String.class);
        assertThat(stringValue, equalTo("my environment file value"));
    }

    @Test
    @Timeout(value = 10)
    public void test_environment_file_property_override_with_an_environment_variable() {


    }
}

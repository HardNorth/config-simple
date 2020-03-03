package com.github.hardnorth.common.config.feature;

import com.github.hardnorth.common.config.ConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EvnVarOverrideTest {

    @Test
    public void test_environment_file_property_override_with_an_environment_variable() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "value_override/env_override");

        ConfigLoader loader = new ConfigLoader(props, getClass().getClassLoader());

        String stringValue = loader.get().getProperty("TEST_ENV_VALUE", String.class);
        assertThat(stringValue, equalTo("my environment value"));
    }
}

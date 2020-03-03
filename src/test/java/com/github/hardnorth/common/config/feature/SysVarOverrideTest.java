package com.github.hardnorth.common.config.feature;

import com.github.hardnorth.common.config.ConfigLoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SysVarOverrideTest {

    @Test
    public void test_environment_variable_property_override_with_a_system_variable() {
        System.setProperty("TEST_SYS_VALUE", "my system value");

        ConfigLoader loader = new ConfigLoader(getClass().getClassLoader());

        String stringValue = loader.get().getProperty("TEST_SYS_VALUE", String.class);
        assertThat(stringValue, equalTo("my system value"));
    }
}

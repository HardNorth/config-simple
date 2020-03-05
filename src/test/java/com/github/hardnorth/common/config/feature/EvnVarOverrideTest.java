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
package com.github.hardnorth.common.config.feature;

import com.github.hardnorth.common.config.ConfigLoader;
import com.github.hardnorth.common.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EvnVarOverrideTest {

    @Test
    public void test_environment_file_property_override_with_an_environment_variable() {
        Properties props = new Properties();
        props.setProperty(ConfigLoader.ENVIRONMENT_PROPERTY, "value_override/env_override");

        ConfigProvider loader = new ConfigLoader(props, getClass().getClassLoader()).get();

        String stringValue = loader.getProperty("TEST_ENV_VALUE", String.class);
        assertThat(stringValue, equalTo("my environment value"));
    }
}

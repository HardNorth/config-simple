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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SysVarOverrideTest {

    @Test
    public void test_environment_variable_property_override_with_a_system_variable() {
        System.setProperty("TEST_SYS_VALUE", "my system value");

        ConfigProvider loader = new ConfigLoader(getClass().getClassLoader()).get();

        String stringValue = loader.getProperty("TEST_SYS_VALUE", String.class);
        assertThat(stringValue, equalTo("my system value"));
    }
}

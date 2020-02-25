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

/**
 * An interface for reading properties.
 */
public interface ConfigProvider {
    /**
     * Returns property value by name.
     *
     * @param name a name of a property
     * @param type a class of property type which will be applied on the property
     * @param <T>  property type
     * @return property value
     */
    <T> T getProperty(String name, Class<T> type);

    /**
     * Returns property value by name, or default value.
     *
     * @param key          a name of a property
     * @param type         a class of property type which will be applied on the property
     * @param defaultValue that value will be returned if no such property defined
     * @param <T>          property type
     * @return property value
     */
    <T> T getProperty(String key, Class<T> type, T defaultValue);
}

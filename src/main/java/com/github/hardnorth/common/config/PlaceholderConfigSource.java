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

import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class PlaceholderConfigSource implements ConfigurationSource {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^{}]+)}");
    private static final char DEFAULT_VALUE_SEPARATOR = ':';

    private final ConfigurationSource base;

    public PlaceholderConfigSource(final ConfigurationSource source) {
        base = source;
    }

    private static Object resolve(final String placeholder, final Properties source) {
        String placeholderKey = placeholder;
        String value = null;
        int defaultValueIndex = placeholder.indexOf(DEFAULT_VALUE_SEPARATOR);
        if (defaultValueIndex > 0) {
            value = placeholder.substring(defaultValueIndex + 1);
            placeholderKey = placeholder.substring(0, defaultValueIndex);
        }
        if (source.containsKey(placeholderKey)) {
            value = source.getProperty(placeholderKey);
        }
        return value;
    }

    public Properties resolvePlaceholders(final Properties source) {
        Map<String, Object> processingResult = source.entrySet().stream().collect(Collectors.toMap(k -> (String) k.getKey(), v -> {
            Object value = v.getValue();
            boolean valueString = value instanceof String;
            if (!valueString) {
                return Optional.of(value);
            }
            String result = (String) value;
            for (int i = 0; ; i++) {
                Matcher m = PLACEHOLDER_PATTERN.matcher(result);
                if (m.find()) {
                    String placeholder = m.group(1);
                    Object placeholderValue = resolve(placeholder, source);
                    if (placeholderValue == null) {
                        if (m.replaceFirst("").isEmpty()) {
                            return Optional.empty();
                        } else {
                            throw new IllegalArgumentException("Unable to find placeholder value: " + placeholder);
                        }
                    } else {
                        if (m.replaceFirst("").isEmpty()) {
                            return Optional.of(placeholderValue);
                        } else {
                            result = m.replaceFirst(placeholderValue.toString());
                        }
                    }
                } else {
                    break;
                }
            }
            return Optional.of(result);
        })).entrySet().stream().filter(e -> e.getValue().isPresent()).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        Properties result = new Properties();
        result.putAll(processingResult);
        return result;
    }

    @Override
    public Properties getConfiguration(Environment environment) {
        return resolvePlaceholders(base.getConfiguration(environment));
    }

    @Override
    public void reload() {
        base.reload();
    }

    @Override
    public void init() {
        base.init();
    }
}

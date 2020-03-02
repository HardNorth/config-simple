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

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * A {@link ConfigurationSource} which reads property values, finds placeholders and try to resolve them.
 * <p>
 * TODO: Make it more accurate with bash default values?
 */
class PlaceholderConfigSource implements ConfigurationSource {
    public static final char DEFAULT_VALUE_DELIMITER = ':';

    public static final char PLACEHOLDER_KEY = '$';

    private final ConfigurationSource base;

    public PlaceholderConfigSource(final ConfigurationSource source) {
        base = source;
    }

    private static class Lookup implements StringLookup {
        private final Properties source;

        public Lookup(final Properties from) {
            source = from;
        }

        @Override
        public String lookup(String key) {
            return source.getProperty(key);
        }
    }

    public Properties resolvePlaceholders(final Properties source) {
        final Lookup lookup = new Lookup(source);
        final StringSubstitutor ssub = new StringSubstitutor(lookup);
        ssub.setEnableSubstitutionInVariables(true);
        ssub.setValueDelimiter(DEFAULT_VALUE_DELIMITER);
        Map<String, Object> processingResult = source.entrySet().stream().collect(Collectors.toMap(k -> (String) k.getKey(), v -> {
            Object value = v.getValue();
            boolean valueString = value instanceof String;
            if (!valueString) {
                return Optional.of(value);
            }
            String result = (String) value;
            int placeholderIndex = result.indexOf(PLACEHOLDER_KEY);
            if (placeholderIndex >= 0) {
                result = ssub.replace(result);
            }
            return Optional.of(result);
        })).entrySet().stream().filter(e -> e.getValue().isPresent()).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        Properties result = new Properties();
        result.putAll(processingResult);
        return result;
    }

    @Override
    public Properties getConfiguration(final Environment environment) {
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

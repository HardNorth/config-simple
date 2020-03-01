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

import org.apache.commons.lang3.tuple.Pair;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

class PlaceholderConfigSource implements ConfigurationSource {
    public static final char DEFAULT_VALUE_SEPARATOR = ':';

    public static final char PLACEHOLDER_KEY = '$';
    public static final char PLACEHOLDER_EMBRACE_START_KEY = '{';
    public static final char PLACEHOLDER_EMBRACE_END_KEY = '}';
    public static final char PLACEHOLDER_END_KEY = ' ';

    public static final int MAX_RESOLVE_DEPTH = 10;

    private final ConfigurationSource base;

    public PlaceholderConfigSource(final ConfigurationSource source) {
        base = source;
    }

    private static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> shiftPlaceholder(int num, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> placeholder) {
        return Pair.of(Pair.of(num + placeholder.getKey().getLeft(), num + placeholder.getKey().getRight()),
                Pair.of(num + placeholder.getValue().getLeft(), num + placeholder.getValue().getRight()));
    }

    private static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> processBarePlaceholder(final String source, final int placeholderStart) {
        int placeholderEnd = source.indexOf(PLACEHOLDER_END_KEY, placeholderStart + 1);
        if (placeholderEnd <= 0) {
            placeholderEnd = source.length();
        }
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> subPlaceholder =
                findPlaceholder(source.substring(placeholderStart + 1, placeholderEnd));
        if (subPlaceholder != null) {
            return shiftPlaceholder(placeholderStart + 1, subPlaceholder);
        }
        return Pair.of(Pair.of(placeholderStart, placeholderEnd), Pair.of(placeholderStart + 1, placeholderEnd));
    }

    private static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> processEmbracedPlaceholder(final String source, final int placeholderStart) {
        int placeholderEnd = source.indexOf(PLACEHOLDER_EMBRACE_END_KEY, placeholderStart + 2);
        if (placeholderEnd < 0) {
            throw new IllegalArgumentException("Unable to find placeholder closing key, please check your placeholder syntax. Embraced placeholders must be closed.");
        }
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> subPlaceholder =
                findPlaceholder(source.substring(placeholderStart + 2, placeholderEnd + 1));
        if (subPlaceholder != null) {
            return shiftPlaceholder(placeholderStart + 2, subPlaceholder);
        }
        return Pair.of(Pair.of(placeholderStart, placeholderEnd + 1), Pair.of(placeholderStart + 2, placeholderEnd));
    }

    /**
     * I can't use RegEx because for some systems it doesn't escape source Strings and count '$' as group reference.
     *
     * @param source source String to extract placeholder
     * @return {@link Pair} of placeholder first and last index and placeholder value first and last index or null if not found
     */
    private static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> findPlaceholder(final String source) {
        if (source == null || source.length() <= 1) {
            return null;
        }

        int placeholderStart = source.indexOf(PLACEHOLDER_KEY);
        if (placeholderStart < 0 || placeholderStart >= (source.length() - 1)) {
            return null;
        }

        if (source.charAt(placeholderStart + 1) != PLACEHOLDER_EMBRACE_START_KEY) {
            return processBarePlaceholder(source, placeholderStart);
        } else {
            return processEmbracedPlaceholder(source, placeholderStart);
        }
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

    private static String cutOffPlaceholder(final String source, final Pair<Integer, Integer> placeholder) {
        return replacePlaceholder(source, "", placeholder);
    }

    private static String replacePlaceholder(final String source, final String value, final Pair<Integer, Integer> placeholder) {
        return source.substring(0, placeholder.getLeft()) + value + source.substring(placeholder.getRight());
    }

    public Properties resolvePlaceholders(final Properties source) {
        Map<String, Object> processingResult = source.entrySet().stream().collect(Collectors.toMap(k -> (String) k.getKey(), v -> {
            Object value = v.getValue();
            boolean valueString = value instanceof String;
            if (!valueString) {
                return Optional.of(value);
            }
            String result = (String) value;
            while (true) {
                Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> placeholder = findPlaceholder(result);
                if (placeholder != null) {
                    String placeholderValue = result.substring(placeholder.getValue().getLeft(), placeholder.getValue().getRight());
                    Object resolvedPlaceholderValue = resolve(placeholderValue, source);
                    if (resolvedPlaceholderValue == null) {
                        if (cutOffPlaceholder(result, placeholder.getKey()).isEmpty()) {
                            return Optional.empty();
                        } else {
                            throw new IllegalArgumentException("Unable to find placeholder value '" + placeholderValue + "' for string: " + value);
                        }
                    } else {
                        if (cutOffPlaceholder(result, placeholder.getKey()).isEmpty()) {
                            return Optional.of(resolvedPlaceholderValue);
                        } else {
                            result = replacePlaceholder(result, resolvedPlaceholderValue.toString(), placeholder.getKey());
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

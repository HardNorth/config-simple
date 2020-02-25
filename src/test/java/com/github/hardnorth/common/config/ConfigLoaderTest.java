package com.github.hardnorth.common.config;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ConfigLoaderTest {

	@Test
	public void test_default_file_load() {
		ConfigLoader loader = new ConfigLoader();
		String propertyValue = loader.get().getProperty("com.github.hardnorth.common.config.test.file", String.class);
		assertThat(propertyValue, equalTo("default.properties"));
	}
}

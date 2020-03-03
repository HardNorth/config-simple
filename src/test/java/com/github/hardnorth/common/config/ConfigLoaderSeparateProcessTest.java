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

import com.github.hardnorth.common.config.feature.EvnVarOverrideTest;
import com.github.hardnorth.common.config.feature.SysVarOverrideTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.joinWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ConfigLoaderSeparateProcessTest {

    private static final boolean IS_POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    private static String getClasspath() {
        String rawClasspath = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        String currentDir = System.getProperty("user.dir");
        return Arrays.stream(rawClasspath.split(pathSeparator))
                .map((s) -> s.contains(" ") ? IS_POSIX ? Paths.get(currentDir).relativize(Paths.get(s)).toString() : "\"" + s + "\"" : s)
                .collect(Collectors.joining(pathSeparator));
    }

    private static ProcessBuilder getJunitProcess(Class<?> junitClass) throws IOException {
        String fileSeparator = System.getProperty("file.separator");
        String javaHome = System.getProperty("java.home");
        String executablePath = joinWith(fileSeparator, javaHome, "bin", "java");
        File executableFile = new File(executablePath);
        if (!executableFile.exists()) {
            executablePath = executablePath + ".exe";
            executableFile = new File(executablePath);
            if (!executableFile.exists()) {
                throw new IllegalStateException("Unable to find java executable file.");
            }
        }
        List<String> commands = new ArrayList<>();
        commands.add(executablePath);
        commands.add("-classpath");
        commands.add(System.getProperty("java.class.path"));
        commands.add("org.junit.platform.console.ConsoleLauncher");
        commands.add("--fail-if-no-tests");
        commands.add("--disable-ansi-colors");
        commands.add("--disable-banner");
        commands.add("--details=tree");
        commands.add("-c=" + junitClass.getCanonicalName());
        return new ProcessBuilder(commands).inheritIO();
    }

    @Test
    @Timeout(value = 10)
    public void test_environment_file_property_override_with_an_environment_variable() throws IOException, InterruptedException {
        ProcessBuilder pb = getJunitProcess(EvnVarOverrideTest.class);
        pb.environment().put("TEST_ENV_VALUE", "my environment value");
        assertThat("Response code should be '0', means that test completed successfully", pb.start().waitFor(), equalTo(0));
    }

    @Test
    @Timeout(value = 10)
    public void test_environment_variable_property_override_with_a_system_variable() throws IOException, InterruptedException {
        ProcessBuilder pb = getJunitProcess(SysVarOverrideTest.class);
        pb.environment().put("TEST_SYS_VALUE", "my environment value");
        assertThat("Response code should be '0', means that test completed successfully", pb.start().waitFor(), equalTo(0));
    }
}

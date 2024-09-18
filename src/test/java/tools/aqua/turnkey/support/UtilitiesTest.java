/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2024 The TurnKey Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.aqua.turnkey.support;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.util.Lists.list;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static tools.aqua.turnkey.support.Utilities.*;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(PER_CLASS)
class UtilitiesTest {

  private static final String TEST_LIST = "test";
  private static final String TEST_LIST_FIRST = "test.0";
  private static final String TEST_LIST_SECOND = "test.1";
  private static final String TEST_LIST_OUT_OF_BOUNDS = "test.3";

  @Test
  void testListPropertyReading() {
    final Properties properties = new Properties();
    properties.setProperty(TEST_LIST_FIRST, "a");
    properties.setProperty(TEST_LIST_SECOND, "b");

    assertThat(getListProperty(properties, TEST_LIST)).containsExactly("a", "b");
  }

  @Test
  void testListPropertyReadingSkipsOutOfOrder() {
    final Properties properties = new Properties();
    properties.setProperty(TEST_LIST_FIRST, "a");
    properties.setProperty(TEST_LIST_SECOND, "b");
    properties.setProperty(TEST_LIST_OUT_OF_BOUNDS, "c");

    assertThat(getListProperty(properties, TEST_LIST)).containsExactly("a", "b");
  }

  @Test
  void testEmptyListPropertyReading() {
    final Properties properties = new Properties();

    assertThat(getListProperty(properties, TEST_LIST)).isEmpty();
  }

  @Test
  void testSetPropertyReading() {
    final Properties properties = new Properties();
    properties.setProperty(TEST_LIST_FIRST, "a");
    properties.setProperty(TEST_LIST_SECOND, "b");

    assertThat(getSetProperty(properties, TEST_LIST)).containsExactly("a", "b");
  }

  @Test
  void testSetPropertyReadingSkipsOutOfOrder() {
    final Properties properties = new Properties();
    properties.setProperty(TEST_LIST_FIRST, "a");
    properties.setProperty(TEST_LIST_SECOND, "b");
    properties.setProperty(TEST_LIST_OUT_OF_BOUNDS, "c");

    assertThat(getSetProperty(properties, TEST_LIST)).containsExactly("a", "b");
  }

  @Test
  void testEmptySetPropertyReading() {
    final Properties properties = new Properties();

    assertThat(getSetProperty(properties, TEST_LIST)).isEmpty();
  }

  @Test
  void testIterablePropertyWriting() {
    final Properties properties = new Properties();

    setIterableProperty(properties, TEST_LIST, list("a", "b"));

    assertThat(properties).containsOnly(entry(TEST_LIST_FIRST, "a"), entry(TEST_LIST_SECOND, "b"));
  }

  @Test
  void testEmptyIterablePropertyWriting() {
    final Properties properties = new Properties();

    setIterableProperty(properties, TEST_LIST, emptyList());

    assertThat(properties).isEmpty();
  }
}

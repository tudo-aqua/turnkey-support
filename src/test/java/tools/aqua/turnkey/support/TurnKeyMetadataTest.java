/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2025 The TurnKey Authors
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.util.Lists.list;
import static org.assertj.core.util.Sets.set;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(PER_CLASS)
class TurnKeyMetadataTest {

  private static final String LIBRARY_A = "liba.so";
  private static final String LIBRARY_B = "libb.so";
  private static final String LIBRARY_C = "libc.so";
  private static final String SYSTEM_LIBRARY_1 = "libsys.so";
  private static final String SYSTEM_LIBRARY_2 = "libsyx.so";

  @Test
  void testLoadingMetadataWorks() throws IOException {
    final TurnKeyMetadata loaded;
    try (InputStream inputStream = TurnKeyMetadataTest.class.getResourceAsStream("/test.xml")) {
      assert inputStream != null;
      loaded = TurnKeyMetadata.loadFrom(inputStream);
    }

    assertThat(loaded).isNotNull();
    assertThat(loaded)
        .extracting(it -> it.bundledLibraries)
        .isNotNull()
        .asInstanceOf(COLLECTION)
        .containsExactlyInAnyOrder(LIBRARY_A, LIBRARY_B);
    assertThat(loaded)
        .extracting(it -> it.systemLibraries)
        .isNotNull()
        .asInstanceOf(COLLECTION)
        .containsExactlyInAnyOrder(SYSTEM_LIBRARY_1);
    assertThat(loaded)
        .extracting(it -> it.loadCommands)
        .isNotNull()
        .asInstanceOf(LIST)
        .containsExactly(LIBRARY_B, LIBRARY_A);
  }

  @Test
  void testEqualityAndHashCodeWorks() {
    final TurnKeyMetadata base =
        new TurnKeyMetadata(
            set(LIBRARY_A, LIBRARY_B), set(SYSTEM_LIBRARY_1), list(LIBRARY_A, LIBRARY_B));
    final TurnKeyMetadata same =
        new TurnKeyMetadata(
            set(LIBRARY_A, LIBRARY_B), set(SYSTEM_LIBRARY_1), list(LIBRARY_A, LIBRARY_B));
    final TurnKeyMetadata otherBundled =
        new TurnKeyMetadata(
            set(LIBRARY_A, LIBRARY_B, LIBRARY_C),
            set(SYSTEM_LIBRARY_1),
            list(LIBRARY_A, LIBRARY_B));
    final TurnKeyMetadata otherSystem =
        new TurnKeyMetadata(
            set(LIBRARY_A, LIBRARY_B), set(SYSTEM_LIBRARY_2), list(LIBRARY_A, LIBRARY_B));
    final TurnKeyMetadata otherCommands =
        new TurnKeyMetadata(
            set(LIBRARY_A, LIBRARY_B), set(SYSTEM_LIBRARY_2), list(LIBRARY_B, LIBRARY_A));

    assertThat(base)
        .isEqualTo(same)
        .isNotEqualTo(otherBundled)
        .isNotEqualTo(otherSystem)
        .isNotEqualTo(otherCommands);
    assertThat(base).extracting(Object::hashCode).isEqualTo(same.hashCode());
  }

  @Test
  void testStoringMetadataWorks() throws IOException {
    final Properties reference;
    try (InputStream inputStream = TurnKeyMetadataTest.class.getResourceAsStream("/test.xml")) {
      reference = new Properties();
      reference.loadFromXML(inputStream);
    }

    final TurnKeyMetadata metadata =
        new TurnKeyMetadata(
            set(LIBRARY_A, LIBRARY_B), set(SYSTEM_LIBRARY_1), list(LIBRARY_B, LIBRARY_A));
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    metadata.writeTo(outputStream);
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    final Properties metadataLoaded = new Properties();
    metadataLoaded.loadFromXML(inputStream);

    assertThat(metadataLoaded).containsExactlyInAnyOrderEntriesOf(reference);
  }
}

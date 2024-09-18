/*
 * SPDX-License-Identifier: ISC
 *
 * Copyright 2019-2024 The TurnKey Authors
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package tools.aqua.turnkey.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/** Utility method collection. */
final class Utilities {
  private Utilities() {
    throw new AssertionError();
  }

  /**
   * Read a {@link List} from a {@link Properties} object using the following convention: the first
   * element is named {@code key.0}, the second {@code key.1} etc. If {@code key.0} is undefined,
   * return an empty list.
   *
   * @param properties the object to read from.
   * @param key the list key prefix.
   * @return the list entries, never {@code null}.
   */
  static List<String> getListProperty(final Properties properties, final String key) {
    return getCollectionProperty(properties, key, new ArrayList<>());
  }

  /**
   * Read a {@link Set} from a {@link Properties} object using the following convention: the first
   * element is named {@code key.0}, the second {@code key.1} etc. If {@code key.0} is undefined,
   * return an empty list.
   *
   * @param properties the object to read from.
   * @param key the set key prefix.
   * @return the set entries, never {@code null}.
   */
  static Set<String> getSetProperty(final Properties properties, final String key) {
    return getCollectionProperty(properties, key, new LinkedHashSet<>());
  }

  private static <T extends Collection<String>> T getCollectionProperty(
      final Properties properties, final String key, final T aggregator) {
    for (int index = 0; properties.containsKey(key + "." + index); index++) {
      aggregator.add(properties.getProperty(key + "." + index));
    }
    return aggregator;
  }

  /**
   * Write an {@link Iterable} to a {@link Properties} object using the following convention: the
   * first element is named {@code key.0}, the second {@code key.1} etc. If {@code key.0} is
   * undefined, return an empty list.
   *
   * @param properties the object to write to.
   * @param key the iterable key prefix.
   */
  static void setIterableProperty(
      final Properties properties, final String key, final Iterable<String> values) {
    int index = 0;
    for (final String value : values) {
      properties.setProperty(key + "." + index++, value);
    }
  }

  /**
   * Copy an input stream to an output stream. This functionality is provided by the standard
   * library on Java 9+; unfortunately, we target Java 8.
   *
   * @param in the input stream.
   * @param out the output stream.
   * @throws IOException if the read or write operation fails. The stream may be partially written.
   */
  static void copy(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[1 << 13];
    int read;
    while ((read = in.read(buffer, 0, buffer.length)) >= 0) {
      out.write(buffer, 0, read);
    }
  }
}

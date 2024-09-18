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

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static tools.aqua.turnkey.support.Utilities.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/** A metadata bundle for a platform-specific TurnKey distribution. */
public final class TurnKeyMetadata {
  /** The set of bundled library files, read-only. */
  public final Set<String> bundledLibraries;

  /** The set of referenced system library names (i.e., non-bundled dependencies), read-only. */
  public final Set<String> systemLibraries;

  /**
   * The platform-specific list of load commands to successfully load the library, in load order,
   * read-only.
   */
  public final List<String> loadCommands;

  /**
   * Load a metadata bundle from a given stream source. This used a Java Properties XML-based
   * format.
   *
   * @param inputStream the stream to read from.
   * @return the loaded metadata file.
   * @throws IOException if reading failed.
   */
  public static TurnKeyMetadata loadFrom(final InputStream inputStream) throws IOException {
    final Properties properties = new Properties();
    properties.loadFromXML(inputStream);

    final Set<String> bundledLibraries = getSetProperty(properties, "bundled-libraries");
    final Set<String> systemLibraries = getSetProperty(properties, "system-libraries");
    final List<String> loadCommands = getListProperty(properties, "load-commands");

    return new TurnKeyMetadata(bundledLibraries, systemLibraries, loadCommands);
  }

  /**
   * Create a new metadata bundle with the given contents.
   *
   * @param bundledLibraries the new {@link #bundledLibraries}, not copied.
   * @param systemLibraries the new {@link #systemLibraries}, not copied.
   * @param loadCommands the new {@link #loadCommands}, not copied.
   */
  public TurnKeyMetadata(
      final Set<String> bundledLibraries,
      final Set<String> systemLibraries,
      final List<String> loadCommands) {
    this.bundledLibraries = unmodifiableSet(bundledLibraries);
    this.systemLibraries = unmodifiableSet(systemLibraries);
    this.loadCommands = unmodifiableList(loadCommands);
  }

  /**
   * Write this metadata bundle to the given stream. This used a Java Properties XML-based format.
   *
   * @param outputStream the stream to write to.
   * @throws IOException if writing fails.
   */
  public void writeTo(final OutputStream outputStream) throws IOException {
    final Properties properties = new Properties();

    setIterableProperty(properties, "bundled-libraries", bundledLibraries);
    setIterableProperty(properties, "system-libraries", systemLibraries);
    setIterableProperty(properties, "load-commands", loadCommands);

    properties.storeToXML(outputStream, "TurnKey Metadata File");
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TurnKeyMetadata)) {
      return false;
    }
    final TurnKeyMetadata that = (TurnKeyMetadata) obj;
    return Objects.equals(bundledLibraries, that.bundledLibraries)
        && Objects.equals(systemLibraries, that.systemLibraries)
        && Objects.equals(loadCommands, that.loadCommands);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bundledLibraries, systemLibraries, loadCommands);
  }

  @Override
  public String toString() {
    return "TurnKeyMetadata{"
        + "bundledLibraries="
        + bundledLibraries
        + ", systemLibraries="
        + systemLibraries
        + ", loadCommands="
        + loadCommands
        + '}';
  }
}

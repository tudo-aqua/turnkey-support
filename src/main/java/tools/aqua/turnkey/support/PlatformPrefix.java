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

import java.util.Objects;

/**
 * The resolved resource prefix for a given library prefix, operating system and CPU architecture.
 */
final class PlatformPrefix {
  /** The full prefix path. */
  private final String prefix;

  /** A human-readable representation for {@link #toString()}. */
  private final String humanReadable;

  /**
   * Create a new platform prefix from the given components.
   *
   * @param libraryPrefix the library-specific prefix to use when loading files without leading or
   *     terminal {@code /}. This is usually the package name of the library with {@code .} replaced
   *     by {@code /}.
   * @param os the operating system component.
   * @param cpu the CPU architecture component.
   */
  PlatformPrefix(final String libraryPrefix, final OperatingSystem os, final CPUArchitecture cpu) {
    if (libraryPrefix.startsWith("/")) {
      throw new IllegalArgumentException("library prefix must not start with '/'");
    }
    if (libraryPrefix.endsWith("/")) {
      throw new IllegalArgumentException("library prefix must not end with '/'");
    }
    prefix = "/" + libraryPrefix + "/" + os.name + "/" + cpu.name + "/";
    humanReadable =
        "PlatformPrefix{library='" + libraryPrefix + "', os=" + os + ", cpu=" + cpu + "}";
  }

  /**
   * Get the resource path for a file in this prefix. This prepends the prefix to the given relative
   * path.
   *
   * @param file the file to load without a leading {@code /}.
   * @return the resource path (for use with, e.g., {@link Class#getResource(String)} for the file).
   */
  String resolve(final String file) {
    if (file.startsWith("/")) {
      throw new IllegalArgumentException("file must not start with '/'");
    }
    return prefix + file;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PlatformPrefix)) {
      return false;
    }
    final PlatformPrefix that = (PlatformPrefix) o;
    return Objects.equals(prefix, that.prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(prefix);
  }

  @Override
  public String toString() {
    return humanReadable;
  }
}

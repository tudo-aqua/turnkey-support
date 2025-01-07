/*
 * SPDX-License-Identifier: ISC
 *
 * Copyright 2019-2025 The TurnKey Authors
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

import static java.lang.System.getProperty;

/** Supported operating systems. */
enum OperatingSystem {
  /** Mac OS. */
  OS_X("osx"),
  /** Linux. */
  LINUX("linux"),
  /** Microsoft Windows. */
  WINDOWS("windows");

  /** The directory name used for the OS's libraries. */
  final String name;

  /**
   * Construct a new enum entry.
   *
   * @param name the {@link #name}.
   */
  OperatingSystem(final String name) {
    this.name = name;
  }

  /**
   * Identify the current operating system. This uses the {@code os.name} system property.
   *
   * @return the current operating system.
   * @throws UnsupportedPlatformException if the current operating system is not known, i.e.,
   *     unsupported.
   * @see <a
   *     href="https://github.com/openjdk/jdk/blob/master/src/java.base/macosx/native/libjava/java_props_macosx.c">OpenJDK
   *     value definition for macOS</a>
   * @see <a
   *     href="https://github.com/openjdk/jdk/blob/master/src/java.base/unix/native/libjava/java_props_md.c">OpenJDK
   *     value definitions for Unix-like OS</a>
   * @see <a
   *     href="https://github.com/openjdk/jdk/blob/master/src/java.base/windows/native/libjava/java_props_md.c">OpenJDK
   *     value definitions for Windows</a>
   */
  static OperatingSystem identify() {
    final String osName = getProperty("os.name");
    if (osName.equals("Mac OS X")) {
      // hardcoded value for macOS
      return OS_X;
    } else if (osName.equals("Linux")) {
      // on all other Unix-like OS, uname(2) is called and the sysname returned
      return LINUX;
    } else if (osName.startsWith("Windows")) {
      // on Windows, the exact version is identified as "Windows <version>"
      return WINDOWS;
    } else {
      throw new UnsupportedPlatformException("Unsupported operating system: " + osName);
    }
  }
}

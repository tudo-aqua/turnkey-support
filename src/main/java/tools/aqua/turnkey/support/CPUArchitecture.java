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

import static java.lang.System.getProperty;

/** Supported CPU architectures. */
enum CPUArchitecture {
  /** Intel/AMD 32 bit. */
  X86("x86"),
  /** Intel/AMD 64 bit. */
  AMD64("amd64"),
  /** ARMv8 64 bit. */
  AARCH64("aarch64");

  /** The directory name used for the OS's libraries. */
  final String name;

  /**
   * Construct a new enum entry.
   *
   * @param name the {@link #name}.
   */
  CPUArchitecture(final String name) {
    this.name = name;
  }

  /**
   * Identify the CPU architecture used by the JVM. For a 32-bit JVM running on a 64-bit CPU, this
   * will identify a 32-bit CPU. Since the JVM needs lo link against same-architecture libraries,
   * this is the desired behavior. This uses the {@code os.arch} system property.
   *
   * @see <a href="https://github.com/openjdk/jdk/blob/master/make/autoconf/platform.m4">OpenJDK
   *     value definitions for Unix-like OS</a>
   * @see <a
   *     href="https://github.com/openjdk/jdk/blob/master/src/java.base/windows/native/libjava/java_props_md.c">OpenJDK
   *     value definitions for Windows</a>
   * @return the current CPU architecture.
   * @throws UnsupportedPlatformException if the current architecture is not known, i.e.,
   *     unsupported.
   */
  static CPUArchitecture identify() {
    final String osArch = getProperty("os.arch");
    switch (osArch) {
      case "x86":
      case "i386":
        // on Windows and all non-Linux Unices, this is configured as "x86"
        // on Linux, the string is set to "i386" for compatibility
        return X86;
      case "aarch64":
        // always "aarch64"
        return AARCH64;
      case "amd64":
      case "x86_64":
        // on Windows and all non-macOS Unices, this is configured as "amd64"
        // on macOS, the string is set to "x86_64" for compatibility
        return AMD64;
      default:
        throw new UnsupportedPlatformException("Unsupported CPU architecture: " + osArch);
    }
  }
}

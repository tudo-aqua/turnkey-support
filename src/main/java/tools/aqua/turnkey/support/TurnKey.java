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

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.newOutputStream;
import static tools.aqua.turnkey.support.Utilities.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/** Handles automatic unpacking and loading for a bundle of native libraries. */
public final class TurnKey {

  /** The conventional file name for turnkey metadata. */
  public static final String TURNKEY_FILE_NAME = "turnkey.xml";

  /** This class should not be constructed. */
  private TurnKey() {
    throw new AssertionError();
  }

  /**
   * Execute the unpack-and-load algorithm. In essence, this does
   *
   * <ol>
   *   <li>Identify the current OS and CPU architecture the JVM runs on.
   *   <li>Check if matching native libraries are present. If not, abort.
   *   <li>Unpack the native libraries to a temporary directory and schedule the resulting files for
   *       deletion on JVM exit.
   *   <li>Load the native libraries.
   * </ol>
   *
   * <p>This must be called before the first operation that uses native code (e.g., in a static
   * initializer).
   *
   * @param libraryPrefix the library-specific prefix to use when loading files without leading or
   *     terminal {@code /}. This is usually the package name of the library with {@code .} replaced
   *     by {@code /}.
   * @param getResourceAsStream the function to use for loading resources. This is necessary in case
   *     of, e.g., modularity restrictions on the visibility of the metadata and libraries.
   * @throws UnsupportedPlatformException if the current platform is not supported.
   * @throws TurnkeyException if library unpacking or linking fails.
   * @throws TurnkeyException if the TurnKey library distribution is incomplete, indicating a
   *     packaging error.
   */
  public static void load(
      final String libraryPrefix,
      final Function<String, @Nullable InputStream> getResourceAsStream) {
    final OperatingSystem os = OperatingSystem.identify();
    final CPUArchitecture cpu = CPUArchitecture.identify();
    final PlatformPrefix platformPrefix = new PlatformPrefix(libraryPrefix, os, cpu);

    final TurnKeyMetadata metadata =
        getMetadata(platformPrefix.resolve(TURNKEY_FILE_NAME), getResourceAsStream);

    final Path unpackedLibraryDir = getTemporaryLibraryDir();
    for (final String bundledLibrary : metadata.bundledLibraries) {
      unpackFile(
          platformPrefix.resolve(bundledLibrary),
          unpackedLibraryDir.resolve(bundledLibrary),
          getResourceAsStream);
    }

    for (final String library : metadata.loadCommands) {
      System.load(unpackedLibraryDir.resolve(library).toAbsolutePath().toString());
    }
  }

  /**
   * Load a metadata file.
   *
   * @param from the location of the metadata file.
   * @param getResourceAsStream the function to use for loading resources.
   * @return the loaded metadata file.
   * @throws UnsupportedPlatformException if the current platform has no metadata file.
   * @throws TurnkeyException if metadata loading fails.
   */
  private static TurnKeyMetadata getMetadata(
      final String from, final Function<String, @Nullable InputStream> getResourceAsStream) {
    try (InputStream inputStream = getResourceAsStream.apply(from)) {
      if (inputStream == null) {
        throw new UnsupportedPlatformException("No file found at " + from);
      }
      return TurnKeyMetadata.loadFrom(inputStream);
    } catch (final IOException e) {
      throw new TurnkeyException("Failed to load metadata from " + from, e);
    }
  }

  /**
   * Create a temporary directory in the host filesystem to unpack libraries to. The directory will
   * be scheduled for deletion on exit.
   *
   * @return the path to the temporary directory.
   * @throws TurnkeyException if creation fails.
   */
  private static Path getTemporaryLibraryDir() {
    try {
      final Path dir = createTempDirectory("turnkey");
      dir.toFile().deleteOnExit();
      return dir;
    } catch (IOException e) {
      throw new TurnkeyException("Failed to create temporary directory", e);
    }
  }

  /**
   * Extract a single file to a given directory on the file system. The file will be scheduled for
   * deletion on exit.
   *
   * @param source path to the file to extract.
   * @param destination the target file to unpack to.
   * @param getResourceAsStream the function to use for loading resources.
   * @throws TurnkeyException if the named library does not exist or cannot be copied.
   */
  private static void unpackFile(
      final String source,
      final Path destination,
      final Function<String, @Nullable InputStream> getResourceAsStream) {
    destination.toFile().deleteOnExit();

    try (InputStream libraryFile = getResourceAsStream.apply(source)) {
      if (libraryFile == null) {
        throw new TurnkeyException("Missing file at " + source + ", packaging error!");
      }
      try (OutputStream targetFile = newOutputStream(destination)) {
        copy(libraryFile, targetFile);
      }
    } catch (IOException e) {
      throw new TurnkeyException("Failed to unpack " + source, e);
    }
  }
}

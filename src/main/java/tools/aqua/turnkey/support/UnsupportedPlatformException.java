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

/**
 * Thrown to indicate a lack of support for the current platform (i.e., required native libraries do
 * not exist or are not bundled).
 */
public final class UnsupportedPlatformException extends UnsupportedOperationException {
  private static final long serialVersionUID = -2223840751649283382L;

  /**
   * Constructs a {@code UnsupportedPlatformException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public UnsupportedPlatformException(final String message) {
    super(message);
  }
}

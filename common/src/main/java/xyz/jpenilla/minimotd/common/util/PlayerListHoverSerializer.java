/*
 * This file is part of MiniMOTD, licensed under the MIT License.
 *
 * Copyright (c) 2020-2025 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.minimotd.common.util;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jspecify.annotations.NullMarked;

/**
 * Serializes MiniMessage player list hover lines into the plain strings the server
 * list protocol carries for sample player entries.
 *
 * <p>Sample entries are plain strings rather than text components, so each parsed
 * line is flattened to legacy section-sign formatting codes. {@link
 * LegacyComponentSerializer#legacySection()} already downsamples hex colors to the
 * nearest of the 16 named colors, so no separate downsampling pass is needed.</p>
 */
@NullMarked
public final class PlayerListHoverSerializer {
  private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

  private PlayerListHoverSerializer() {
  }

  /**
   * Parses and flattens the given MiniMessage lines, preserving their order.
   *
   * @param lines    MiniMessage lines from the config
   * @param resolver tag resolver to parse the lines with
   * @return the lines as legacy-formatted strings
   */
  public static List<String> serialize(final List<String> lines, final TagResolver resolver) {
    final List<String> serialized = new ArrayList<>(lines.size());
    for (final String line : lines) {
      if (line.isEmpty()) {
        serialized.add("");
        continue;
      }
      final Component parsed = MiniMessage.miniMessage().deserialize(line, resolver);
      serialized.add(LEGACY.serialize(parsed));
    }
    return serialized;
  }
}

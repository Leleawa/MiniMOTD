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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PlayerListHoverSerializerTest {
  private static final String S = "§";

  private static final TagResolver RESOLVER = TagResolver.resolver(
    Placeholder.unparsed("online_players", "7"),
    Placeholder.unparsed("max_players", "99")
  );

  private static List<String> serialize(final String... lines) {
    return PlayerListHoverSerializer.serialize(Arrays.asList(lines), RESOLVER);
  }

  @Test
  void namedColorBecomesLegacyCode() {
    assertEquals(Collections.singletonList(S + "chi"), serialize("<red>hi"));
  }

  @Test
  void decorationIsClosedRatherThanCarriedOver() {
    // Legacy color codes reset decorations, so the serializer has to re-emit the color
    assertEquals(
      Collections.singletonList(S + "c" + S + "lA" + S + "c " + S + "7B"),
      serialize("<red><bold>A</bold> <gray>B")
    );
  }

  @Test
  void placeholdersAreResolved() {
    assertEquals(
      Collections.singletonList(S + "7Online " + S + "8| " + S + "b7" + S + "7/" + S + "b99"),
      serialize("<gray>Online <dark_gray>| <aqua><online_players><gray>/<aqua><max_players>")
    );
  }

  @Test
  void hexColorIsDownsampledToNamedColor() {
    final List<String> out = serialize("<#ff8800>hex");
    assertEquals(Collections.singletonList(S + "6hex"), out);
    assertFalse(out.get(0).contains(S + "x"), "hex colors must not survive as " + S + "x sequences");
  }

  @Test
  void emptyLineStaysEmpty() {
    assertEquals(Arrays.asList("", S + "chi", ""), serialize("", "<red>hi", ""));
  }

  @Test
  void orderAndDuplicatesArePreserved() {
    final String rule = S + "8" + S + "m-----";
    assertEquals(
      Arrays.asList(rule, S + "aMiddle", rule),
      serialize("<dark_gray><strikethrough>-----", "<green>Middle", "<dark_gray><strikethrough>-----")
    );
  }

  @Test
  void atlasTagIsLeftAsLiteralText() {
    // Sample entries are plain strings, so an object component would be silently dropped.
    // The atlas resolver is deliberately not supplied for hover lines.
    assertEquals(Collections.singletonList("<atlas:diamond> tail"), serialize("<atlas:diamond> tail"));
  }
}

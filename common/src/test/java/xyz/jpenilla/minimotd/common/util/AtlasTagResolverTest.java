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

import java.util.LinkedHashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Test;
import xyz.jpenilla.minimotd.common.Constants;
import xyz.jpenilla.minimotd.common.config.MOTDConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtlasTagResolverTest {
  private static final int MODERN = Constants.MINECRAFT_ITEMS_ATLAS_PROTOCOL_VERSION;
  private static final int NO_ITEMS_ATLAS = Constants.MINECRAFT_ITEMS_ATLAS_PROTOCOL_VERSION - 1;
  private static final int LEGACY = Constants.MINECRAFT_1_21_9_PROTOCOL_VERSION - 1;

  private static final Map<String, MOTDConfig.AtlasAlias> ALIASES = aliases();

  private static Map<String, MOTDConfig.AtlasAlias> aliases() {
    final Map<String, MOTDConfig.AtlasAlias> map = new LinkedHashMap<>();
    map.put("gem", new MOTDConfig.AtlasAlias("minecraft:blocks", "block/diamond_block", "<aqua>[Gem]"));
    map.put("pork", new MOTDConfig.AtlasAlias("minecraft:items", "item/porkchop", "<red>[Pork]"));
    map.put("blank", new MOTDConfig.AtlasAlias("minecraft:blocks", "block/stone", ""));
    return map;
  }

  private static String json(final String input, final int protocolVersion) {
    final Component parsed = MiniMessage.miniMessage()
      .deserialize(input, AtlasTagResolver.create(ALIASES, protocolVersion));
    return GsonComponentSerializer.gson().serialize(parsed);
  }

  @Test
  void modernClientGetsSpriteObject() {
    assertEquals(
      "{\"extra\":[{\"sprite\":\"minecraft:block/diamond_block\"},\"b\"],\"text\":\"a\"}",
      json("a<atlas:gem>b", MODERN)
    );
  }

  @Test
  void nonDefaultAtlasIsEmitted() {
    assertEquals(
      "{\"extra\":[{\"atlas\":\"minecraft:items\",\"sprite\":\"minecraft:item/porkchop\"},\"b\"],\"text\":\"a\"}",
      json("a<atlas:pork>b", MODERN)
    );
  }

  @Test
  void itemsAtlasIsDowngradedToBlocksBeforeItIsRegistered() {
    // 773 (1.21.9/1.21.10) has no minecraft:items atlas; those sprites still live in minecraft:blocks,
    // which is the default atlas and so is omitted from the serialized form.
    assertEquals(
      "{\"extra\":[{\"sprite\":\"minecraft:item/porkchop\"},\"b\"],\"text\":\"a\"}",
      json("a<atlas:pork>b", NO_ITEMS_ATLAS)
    );
  }

  @Test
  void otherAtlasesAreUntouchedBeforeTheItemsAtlasExists() {
    assertEquals(
      json("a<atlas:gem>b", MODERN),
      json("a<atlas:gem>b", NO_ITEMS_ATLAS)
    );
  }

  @Test
  void legacyClientGetsFallbackInstead() {
    assertEquals(
      "{\"extra\":[{\"color\":\"aqua\",\"text\":\"[Gem]\"},\"b\"],\"text\":\"a\"}",
      json("a<atlas:gem>b", LEGACY)
    );
  }

  @Test
  void legacyOutputContainsNothingAnOldClientCannotRead() {
    for (final String line : new String[]{"<atlas:gem><atlas:pork><atlas:blank>", "x<atlas:pork>y"}) {
      final String out = json(line, LEGACY);
      assertFalse(out.contains("sprite"), out);
      assertFalse(out.contains("atlas"), out);
      assertFalse(out.contains("object"), out);
    }
  }

  @Test
  void emptyFallbackLeavesOnlySurroundingText() {
    assertEquals("\"ab\"", json("a<atlas:blank>b", LEGACY));
  }

  @Test
  void tagIsSelfClosingAndDoesNotSwallowFollowingContent() {
    assertTrue(json("<atlas:gem>tail", MODERN).contains("tail"));
    assertTrue(json("<atlas:gem>tail", LEGACY).contains("tail"));
  }

  @Test
  void unknownAliasDegradesToLiteralText() {
    assertEquals("\"<atlas:nope>x\"", json("<atlas:nope>x", MODERN));
    assertEquals("\"<atlas>x\"", json("<atlas>x", MODERN));
  }
}

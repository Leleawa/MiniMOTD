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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import xyz.jpenilla.minimotd.common.Constants;
import xyz.jpenilla.minimotd.common.config.MOTDConfig;

@NullMarked
public final class AtlasTagResolver {
  public static final String TAG_NAME = "atlas";

  private static final Pattern TAG_USAGE = Pattern.compile("<" + TAG_NAME + ":([^<>:]+)>");

  private AtlasTagResolver() {
  }

  public static void validate(final MOTDConfig config, final String configName, final Logger logger) {
    final Map<String, MOTDConfig.AtlasAlias> aliases = config.atlasAliases();

    for (final Map.Entry<String, MOTDConfig.AtlasAlias> entry : aliases.entrySet()) {
      final MOTDConfig.AtlasAlias alias = entry.getValue();
      try {
        Key.key(alias.atlas());
        Key.key(alias.sprite());
      } catch (final InvalidKeyException ex) {
        logger.warn(
          "[{}] Atlas alias '{}' has an invalid atlas or sprite key (atlas='{}', sprite='{}'): {}",
          configName, entry.getKey(), alias.atlas(), alias.sprite(), ex.getMessage()
        );
      }
    }

    for (final MOTDConfig.MOTD motd : config.motds()) {
      for (final String line : new String[]{motd.line1(), motd.line2()}) {
        final Matcher matcher = TAG_USAGE.matcher(line);
        while (matcher.find()) {
          final String name = matcher.group(1);
          if (!aliases.containsKey(name)) {
            logger.warn(
              "[{}] MOTD line references unknown atlas alias '{}'; it will show up as literal text. Line: {}",
              configName, name, line
            );
          }
        }
      }
    }
  }

  public static TagResolver create(final Map<String, MOTDConfig.AtlasAlias> aliases, final int protocolVersion) {
    final boolean spritesSupported = protocolVersion >= Constants.MINECRAFT_1_21_9_PROTOCOL_VERSION;

    return TagResolver.resolver(TAG_NAME, (args, ctx) -> {
      final String aliasName = args.popOr("An atlas alias name is required, ex: <atlas:myAlias>").value();
      final MOTDConfig.@Nullable AtlasAlias alias = aliases.get(aliasName);

      if (alias == null) {
        throw ctx.newException("Unknown atlas alias '" + aliasName + "'. Define it under 'atlas-aliases' in the config.");
      }

      if (!spritesSupported) {
        return Tag.selfClosingInserting(ctx.deserialize(alias.fallback()));
      }

      final Key atlas;
      final Key sprite;
      try {
        atlas = Key.key(alias.atlas());
        sprite = Key.key(alias.sprite());
      } catch (final InvalidKeyException ex) {
        throw ctx.newException("Atlas alias '" + aliasName + "' has an invalid atlas or sprite key", ex, args);
      }

      return Tag.selfClosingInserting(Component.object(ObjectContents.sprite(atlas, sprite)));
    });
  }
}

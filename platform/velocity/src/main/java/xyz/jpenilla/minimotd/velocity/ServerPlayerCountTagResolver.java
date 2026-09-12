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
package xyz.jpenilla.minimotd.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Resolves {@code <server_players:'serverName'>} to the number of players connected to
 * that backend server.
 *
 * <p>An unknown server name resolves to {@code 0} rather than failing, so that a typo in
 * the config cannot break the whole ping response.</p>
 */
@NullMarked
public final class ServerPlayerCountTagResolver {
  public static final String TAG_NAME = "server_players";

  private ServerPlayerCountTagResolver() {
  }

  public static TagResolver create(final ProxyServer proxy) {
    return TagResolver.resolver(TAG_NAME, (args, ctx) -> {
      final String serverName = args.popOr("A server name is required, ex: <server_players:'lobby'>").value();
      final @Nullable RegisteredServer server = proxy.getServer(serverName).orElse(null);
      final int count = server == null ? 0 : server.getPlayersConnected().size();
      return Tag.preProcessParsed(Integer.toString(count));
    });
  }
}

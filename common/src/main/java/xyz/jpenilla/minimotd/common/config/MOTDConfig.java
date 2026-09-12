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
package xyz.jpenilla.minimotd.common.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import xyz.jpenilla.minimotd.common.PingResponse;

import static xyz.jpenilla.minimotd.common.PingResponse.PlayerCount.playerCount;

@ConfigSerializable
public final class MOTDConfig {

  public MOTDConfig() {
    this(
      new MOTD(),
      new MOTD("<blue>Another <bold><red>MOTD", "<italic><underlined><gradient:red:green>much wow")
    );
  }

  public MOTDConfig(final @NonNull MOTD @NonNull ... defaults) {
    this.motds.addAll(Arrays.asList(defaults));
  }

  @Comment("The list of MOTDs to display\n"
    + "\n"
    + " - Supported placeholders: <online_players>, <max_players>\n"
    + " - Putting more than one will cause one to be randomly chosen each refresh")
  private final List<MOTD> motds = new ArrayList<>();

  @Comment("Named aliases for atlas sprite objects, usable in MOTD lines as <atlas:name>\n"
    + "\n"
    + " - Requires a Minecraft 1.21.9+ client; the 'object' text component did not exist before that.\n"
    + " - Older clients cannot deserialize object components and silently discard the WHOLE MOTD,\n"
    + "   so for them each <atlas:...> tag is replaced with that alias' 'fallback' string.\n"
    + " - 'fallback' is parsed as MiniMessage, so it may contain colors, or be empty for nothing.")
  private Map<String, AtlasAlias> atlasAliases = defaultAtlasAliases();

  @Comment("Enable MOTD-related features")
  private boolean motdEnabled = true;

  @Comment("Enable server list icon related features")
  private boolean iconEnabled = true;

  private PlayerCountSettings playerCountSettings = new PlayerCountSettings();

  private static Map<String, AtlasAlias> defaultAtlasAliases() {
    final Map<String, AtlasAlias> map = new LinkedHashMap<>();
    map.put("diamond", new AtlasAlias("minecraft:blocks", "block/diamond_block", "<aqua>[Diamond]"));
    return map;
  }

  @ConfigSerializable
  public static final class AtlasAlias {

    public AtlasAlias() {
    }

    public AtlasAlias(final @NonNull String atlas, final @NonNull String sprite, final @NonNull String fallback) {
      this.atlas = atlas;
      this.sprite = sprite;
      this.fallback = fallback;
    }

    @Comment("The sprite atlas to pull from\n"
      + "    ex: atlas=\"minecraft:blocks\" or atlas=\"minecraft:items\"")
    private String atlas = "minecraft:blocks";

    @Comment("The sprite within that atlas\n"
      + "    ex: sprite=\"block/diamond_block\" or sprite=\"item/porkchop\"")
    private String sprite = "block/diamond_block";

    @Comment("MiniMessage string shown in place of this sprite on clients older than 1.21.9")
    private String fallback = "";

    public @NonNull String atlas() {
      return this.atlas;
    }

    public @NonNull String sprite() {
      return this.sprite;
    }

    public @NonNull String fallback() {
      return this.fallback;
    }

  }

  @ConfigSerializable
  public static final class MOTD {

    public MOTD() {
    }

    public MOTD(final @NonNull String line1, final @NonNull String line2) {
      this.line1 = line1;
      this.line2 = line2;
    }

    private String line1 = "<rainbow>MiniMOTD Default";

    private String line2 = "MiniMessage <gradient:blue:red>Gradients";

    @Comment("Set the icon to use with this MOTD\n"
      + "  Either use 'random' to randomly choose an icon, or use the name\n"
      + "  of a file in the icons folder (excluding the '.png' extension)\n"
      + "    ex: icon=\"myIconFile\"")
    private String icon = "random";

    public @NonNull String line1() {
      return this.line1;
    }

    public @NonNull String line2() {
      return this.line2;
    }

    public @NonNull String icon() {
      return this.icon;
    }

  }

  @ConfigSerializable
  public static final class PlayerCountSettings {

    @Comment("Enable modification of the max player count")
    private boolean maxPlayersEnabled = true;

    @Comment("Changes the Max Players value")
    private int maxPlayers = 69;

    @Comment("Setting this to true will disable the hover text showing online player usernames")
    private boolean disablePlayerListHover = false;

    @Comment("Setting this to true will disable the player list hover (same as 'disable-player-list-hover'),\n"
      + "but will also cause the player count to appear as '???'")
    private boolean hidePlayerCount = false;

    @Comment("Settings for the fake player count feature")
    private FakePlayers fakePlayers = new FakePlayers();

    @Comment("Changes the Max Players to be X more than the online players\n"
      + "ex: x=3 -> 16/19 players online.")
    private JustXMore justXMoreSettings = new JustXMore();

    @Comment("Should the displayed online player count be allowed to exceed the displayed maximum player count?\n"
      + "If false, the online player count will be capped at the maximum player count")
    private boolean allowExceedingMaximum = false;

    @Comment("The list of server names that affect player counts/listing.\n"
      + "Only applicable when running the plugin on a proxy (Velocity or Waterfall/Bungeecord).\n"
      + "When set to an empty list, the default count & list as determined by the proxy will be used.")
    private final List<String> servers = new ArrayList<>();

    @Comment("Replaces the player list hover with custom lines of text, instead of\n"
      + "the usernames of online players.\n"
      + "Only applicable when running the plugin on a proxy (Velocity).\n"
      + "\n"
      + " - Lines are parsed as MiniMessage, then downsampled to legacy formatting codes.\n"
      + "   The protocol carries hover entries as plain strings rather than components, so\n"
      + "   hex colors are approximated to the nearest of the 16 legacy colors, gradients\n"
      + "   become one color code per character, and <atlas:...> is NOT supported here.\n"
      + " - Supported placeholders: <online_players>, <max_players>, <server_players:'serverName'>\n"
      + " - Ignored when 'disable-player-list-hover' or 'hide-player-count' is true.")
    private PlayerListHover playerListHover = new PlayerListHover();

    @ConfigSerializable
    public static final class PlayerListHover {

      @Comment("Enable the custom player list hover")
      private boolean playerListHoverEnabled = false;

      @Comment("The lines to display, in order. An empty string renders as a blank line.")
      private List<String> lines = defaultLines();

      private static List<String> defaultLines() {
        return new ArrayList<>(Arrays.asList(
          "<dark_gray><strikethrough>---------------------",
          "<green><bold>Players",
          "<white>Lobby <dark_gray>\u00bb <aqua><server_players:'lobby'>",
          "<dark_gray><strikethrough>---------------------"
        ));
      }

    }

    @ConfigSerializable
    public static final class JustXMore {

      @Comment("Enable this feature")
      private boolean justXMoreEnabled = false;

      private int xValue = 3;

    }

    @ConfigSerializable
    public static final class FakePlayers {

      @Comment("Enable fake player count feature")
      private boolean fakePlayersEnabled = false;

      @Comment("Modes: add, constant, minimum, random, percent\n"
        + "\n"
        + " - add: This many fake players will be added\n"
        + "     ex: fake-players=\"3\"\n"
        + " - constant: A constant value for the player count\n"
        + "     ex: fake-players=\"=42\"\n"
        + " - minimum: The minimum bound of the player count\n"
        + "     ex: fake-players=\"7+\"\n"
        + " - random: A random number of fake players in this range will be added\n"
        + "     ex: fake-players=\"3:6\"\n"
        + " - percent: The player count will be inflated by this much, rounding up\n"
        + "     ex: fake-players=\"25%\"")
      private PlayerCountModifier fakePlayers = PlayerCountModifier.parse("25%");

    }

  }

  public List<String> targetServers() {
    return this.playerCountSettings.servers;
  }

  public boolean iconEnabled() {
    return this.iconEnabled;
  }

  public @NonNull List<MOTD> motds() {
    return this.motds;
  }

  public boolean motdEnabled() {
    return this.motdEnabled;
  }

  public @NonNull Map<String, AtlasAlias> atlasAliases() {
    return this.atlasAliases;
  }

  public boolean disablePlayerListHover() {
    return this.playerCountSettings.disablePlayerListHover;
  }

  public boolean hidePlayerCount() {
    return this.playerCountSettings.hidePlayerCount;
  }

  public boolean playerListHoverEnabled() {
    return this.playerCountSettings.playerListHover.playerListHoverEnabled;
  }

  public @NonNull List<String> playerListHoverLines() {
    return this.playerCountSettings.playerListHover.lines;
  }

  private @NonNull PlayerCountModifier playerCountModifier() {
    return this.playerCountSettings.fakePlayers.fakePlayers;
  }

  private int calculateOnlinePlayers(final int onlinePlayers) {
    if (this.playerCountSettings.fakePlayers.fakePlayersEnabled) {
      return this.playerCountModifier().apply(onlinePlayers);
    }
    return onlinePlayers;
  }

  private int calculateMaxPlayers(final int onlinePlayers, final int maxPlayers) {
    if (this.playerCountSettings.maxPlayersEnabled) {
      if (this.playerCountSettings.justXMoreSettings.justXMoreEnabled) {
        return onlinePlayers + this.playerCountSettings.justXMoreSettings.xValue;
      }
      return this.playerCountSettings.maxPlayers;
    }
    return maxPlayers;
  }

  public PingResponse.@NonNull PlayerCount modifyPlayerCount(final int onlinePlayers, final int maxPlayers) {
    final int online = this.calculateOnlinePlayers(onlinePlayers);
    final int max = this.calculateMaxPlayers(online, maxPlayers);
    if (!this.playerCountSettings.allowExceedingMaximum) {
      return playerCount(Math.min(online, max), max);
    }
    return playerCount(online, max);
  }
}

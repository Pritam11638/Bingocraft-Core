package com.pritam.bingocraft.api.sidebar;

import com.pritam.bingocraft.api.utils.Message;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

/**
 * Abstract base class for creating and managing sidebar scoreboards in Minecraft.
 *
 * <p>This class provides a framework for displaying dynamic scoreboards to players with customizable
 * titles and lines. It supports both literal lines (simple text) and dynamic lines (with prefix/suffix
 * that can be updated independently).</p>
 *
 * <h3>Placeholders</h3>
 * <p>All {@link Message} components used in this class receive the following placeholders:</p>
 * <ul>
 *   <li><code>player_name</code> - The name of the player viewing the scoreboard</li>
 * </ul>
 *
 * <h3>Line Types</h3>
 * <ul>
 *   <li>{@link LiteralLine} - Simple text lines that can be updated entirely</li>
 *   <li>{@link DynamicLine} - Lines with separate prefix and suffix components for more granular updates</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <p>Extend this class and implement the {@link Runnable#run()} method to define when and how
 * the scoreboard should be updated. The run method is automatically called to refresh scoreboards
 * for all assigned players.</p>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * public class GameSidebar extends SidebarView {
 *     public GameSidebar() {
 *         super("game_scoreboard");
 *         setTitle(new Message(ph -> Component.text("Game Stats")));
 *         setLine(0, new LiteralLine(new Message(ph ->
 *             Component.text("Player: " + ph.get("player_name")))));
 *     }
 *
 *     // Additional game-specific logic...
 * }
 * }</pre>
 *
 * @author Pritam
 * @since 1.0.0
 */
public abstract class SidebarView {
    /**
     * Unique identifier for this scoreboard instance.
     * Used to distinguish between different scoreboards on the same player.
     */
    @Getter
    private final String SCOREBOARD_ID;

    /**
     * Set of player UUIDs that are currently assigned to view this scoreboard.
     */
    private final Set<UUID> players;

    /**
     * The title message displayed at the top of the scoreboard.
     * Receives placeholders including the viewing player's name.
     */
    private Message title;

    /**
     * Array of lines displayed on the scoreboard.
     * Maximum of 15 lines are supported by Minecraft scoreboards.
     */
    private final Line[] lines;

    /**
     * Unique color-coded entries used for scoreboard line identification.
     * These are used internally to manage scoreboard entries and should not be modified.
     */
    private static final List<String> uniqueEntries = List.of(
            ChatColor.COLOR_CHAR + "0", ChatColor.COLOR_CHAR + "1", ChatColor.COLOR_CHAR + "2",
            ChatColor.COLOR_CHAR + "3", ChatColor.COLOR_CHAR + "4", ChatColor.COLOR_CHAR + "5",
            ChatColor.COLOR_CHAR + "6", ChatColor.COLOR_CHAR + "7", ChatColor.COLOR_CHAR + "8",
            ChatColor.COLOR_CHAR + "9", ChatColor.COLOR_CHAR + "a", ChatColor.COLOR_CHAR + "b",
            ChatColor.COLOR_CHAR + "c", ChatColor.COLOR_CHAR + "d", ChatColor.COLOR_CHAR + "e"
    );

    /**
     * Serializer for converting Adventure components to legacy text format.
     * Used for compatibility with Bukkit's scoreboard system.
     */
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    /**
     * Cache of the last literal line content for each player.
     * Used to optimize updates by only changing lines that have actually changed.
     */
    private final Map<UUID, Map<Integer, String>> lastLiteralLines = new HashMap<>();

    /**
     * Creates a new SidebarView with the specified scoreboard identifier.
     *
     * <p>Initializes the scoreboard with the given ID, an empty player set, a default title,
     * and 15 empty lines ready for content.</p>
     *
     * @param scoreboardId Unique identifier for this scoreboard instance. Must be unique
     *                    across all scoreboards to prevent conflicts.
     */
    public SidebarView(String scoreboardId) {
        this.SCOREBOARD_ID = scoreboardId;
        this.players = new HashSet<>();
        this.title = new Message(ph -> Component.text(scoreboardId));
        this.lines = new Line[15];

        for (int i = 0; i < lines.length; i++) {
            lines[i] = new LiteralLine(new Message(ph -> Component.empty()));
        }
    }

    /**
     * Assigns a player to view this scoreboard.
     *
     * <p>The player will receive this scoreboard on their next update cycle.
     * If the player is already assigned, this method has no effect.</p>
     *
     * @param playerId UUID of the player to assign to this scoreboard
     */
    public void assignPlayer(UUID playerId) {
        players.add(playerId);
    }

    /**
     * Updates the scoreboard title for all assigned players.
     *
     * <p>The title message will receive placeholders including the viewing player's name.
     * This method immediately updates the title for all online players currently viewing
     * this scoreboard.</p>
     *
     * @param title New title message. Receives placeholders: player_name
     */
    protected void setTitle(Message title) {
        this.title = title;
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                Objective obj = player.getScoreboard().getObjective(SCOREBOARD_ID);
                if (obj != null) {
                    obj.displayName(title.getComponent(Map.of("player_name", player.getName())));
                }
            }
        }
    }

    /**
     * Sets the content of a specific line on the scoreboard.
     *
     * <p>Lines are numbered from 0 (top) to 14 (bottom). The line content will be applied
     * to all assigned players on the next update cycle.</p>
     *
     * @param lineNumber Line index (0-14) where 0 is the top line
     * @param line Line content, either {@link LiteralLine} or {@link DynamicLine}
     * @throws IndexOutOfBoundsException if lineNumber is not between 0 and 14
     */
    protected void setLine(int lineNumber, Line line) {
        if (lineNumber < 0 || lineNumber >= lines.length) {
            throw new IndexOutOfBoundsException("Line number must be between 0 and 14");
        }
        lines[lineNumber] = line;
    }

    /**
     * Updates the scoreboard for all assigned players.
     *
     * <p>This method is called automatically and performs the following operations:</p>
     * <ul>
     *   <li>Creates new scoreboards for players who don't have one</li>
     *   <li>Updates existing scoreboards with current content</li>
     *   <li>Optimizes updates by only changing modified lines</li>
     *   <li>Skips offline players automatically</li>
     * </ul>
     *
     * <p>All {@link Message} components are evaluated with placeholders including
     * the current player's name.</p>
     */
    public void tick() {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                if (player.getScoreboard().getObjective(SCOREBOARD_ID) == null) {
                    createNewScoreboard(player);
                } else {
                    updateScoreboard(player);
                }
            }
        }
    }

    /**
     * Creates a completely new scoreboard for the specified player.
     *
     * <p>This method is called when a player doesn't have an existing scoreboard
     * with the current scoreboard ID. It creates all lines from scratch and applies
     * the current title and line configuration.</p>
     *
     * @param player Player to create the scoreboard for
     */
    private void createNewScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(SCOREBOARD_ID, Criteria.DUMMY,
                title.getComponent(Map.of("player_name", player.getName())));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Map<Integer, String> lastLines = new HashMap<>();

        for (int i = 0, score = 15; i < lines.length && score > 0; i++, score--) {
            Line line = lines[i];
            String entry = uniqueEntries.get(i);

            if (line instanceof LiteralLine literal) {
                Component comp = literal.message().getComponent(Map.of("player_name", player.getName()));

                if (comp.equals(Component.empty())) continue;

                String serialized = SERIALIZER.serialize(comp);
                objective.getScore(serialized).setScore(score);
                lastLines.put(i, serialized);

            } else if (line instanceof DynamicLine dyn) {
                Component prefix = dyn.prefix.getComponent(Map.of("player_name", player.getName()));
                Component suffix = dyn.suffix.getComponent(Map.of("player_name", player.getName()));

                if (prefix.equals(Component.empty()) && suffix.equals(Component.empty())) continue;

                Team team = scoreboard.registerNewTeam(dyn.UID);
                team.addEntry(entry);
                team.prefix(prefix);
                team.suffix(suffix);
                objective.getScore(entry).setScore(score--);
            }
        }

        lastLiteralLines.put(player.getUniqueId(), lastLines);
        player.setScoreboard(scoreboard);
    }

    /**
     * Updates an existing scoreboard for the specified player.
     *
     * <p>This method optimizes performance by only updating lines that have changed
     * since the last update. It handles both literal and dynamic line types appropriately.</p>
     *
     * @param player Player whose scoreboard should be updated
     */
    private void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(SCOREBOARD_ID);
        if (objective == null) return;

        Map<Integer, String> lastLines = lastLiteralLines.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>());

        for (int i = 0; i < lines.length; i++) {
            Line line = lines[i];

            if (line instanceof LiteralLine(Message message)) {
                Component comp = message.getComponent(Map.of("player_name", player.getName()));
                String serialized = SERIALIZER.serialize(comp);

                String prev = lastLines.get(i);

                if (comp.equals(Component.empty())) {
                    if (prev != null) {
                        scoreboard.resetScores(prev);
                        lastLines.remove(i);
                    }
                    continue;
                }

                if (serialized.equals(prev)) continue;

                if (prev != null) scoreboard.resetScores(prev);

                objective.getScore(serialized).setScore(15 - i);
                lastLines.put(i, serialized);

            } else if (line instanceof DynamicLine(String uid, Message prefixMessage, Message suffixMessage)) {
                Team team = scoreboard.getTeam(uid);
                if (team == null) continue;

                Component prefix = prefixMessage.getComponent(Map.of("player_name", player.getName()));
                Component suffix = suffixMessage.getComponent(Map.of("player_name", player.getName()));

                if (prefix.equals(Component.empty()) && suffix.equals(Component.empty())) {
                    team.prefix(Component.empty());
                    team.suffix(Component.empty());
                    continue;
                }

                team.prefix(prefix);
                team.suffix(suffix);
            }
        }
    }

    /**
     * Base interface for all scoreboard line types.
     *
     * <p>Implementations define different ways of displaying content on scoreboard lines.</p>
     */
    public interface Line {}

    /**
     * A simple line containing a single message component.
     *
     * <p>Literal lines are best for content that changes entirely when updated.
     * The entire line content is replaced when the message changes.</p>
     *
     * @param message Message component for this line. Receives placeholders: player_name
     */
    public record LiteralLine(Message message) implements Line {}

    /**
     * A line with separate prefix and suffix components for granular updates.
     *
     * <p>Dynamic lines allow for more efficient updates when only part of the line
     * content needs to change. The prefix and suffix can be updated independently.</p>
     *
     * @param UID Unique identifier for this dynamic line's team
     * @param prefix Left portion of the line. Receives placeholders: player_name
     * @param suffix Right portion of the line. Receives placeholders: player_name
     */
    public record DynamicLine(String UID, Message prefix, Message suffix) implements Line {}
}


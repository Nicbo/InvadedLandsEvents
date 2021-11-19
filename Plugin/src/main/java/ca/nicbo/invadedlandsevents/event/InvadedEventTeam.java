package ca.nicbo.invadedlandsevents.event;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A team of players in the {@link InvadedEvent}.
 *
 * @author Nicbo
 */
public class InvadedEventTeam implements Iterable<Player> {
    private final String name;
    private final Set<Player> players;
    private Set<Player> playersBackup;

    public InvadedEventTeam(String name) {
        this(name, new HashSet<>());
    }

    public InvadedEventTeam(String name, Set<Player> players) {
        Validate.checkArgumentNotNull(name, "name");
        Validate.checkArgumentNotNull(players, "players");
        this.name = name;
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public void add(Player player) {
        Validate.checkNotNull(player, "player");
        players.add(player);
    }

    public void remove(Player player) {
        Validate.checkNotNull(player, "player");
        players.remove(player);
    }

    public boolean contains(Player player) {
        Validate.checkNotNull(player, "player");
        return players.contains(player);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public int size() {
        return players.size();
    }

    public void clear() {
        players.clear();
    }

    public void backup() {
        playersBackup = new HashSet<>(players);
    }

    public Set<Player> getPlayersBackup() {
        return playersBackup != null ?
                Collections.unmodifiableSet(playersBackup) :
                Collections.emptySet();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, players, playersBackup);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof InvadedEventTeam)) {
            return false;
        }

        InvadedEventTeam other = (InvadedEventTeam) obj;
        return name.equals(other.name) && players.equals(other.players) && Objects.equals(playersBackup, other.playersBackup);
    }

    @Override
    public Iterator<Player> iterator() {
        return players.iterator();
    }

    public Stream<Player> stream() {
        return players.stream();
    }

    public static InvadedEventTeam unmodifiableOf(InvadedEventTeam original) {
        Validate.checkArgumentNotNull(original, "original");
        return new InvadedEventUnmodifiableTeam(original);
    }

    private static final class InvadedEventUnmodifiableTeam extends InvadedEventTeam {
        private InvadedEventUnmodifiableTeam(InvadedEventTeam original) {
            super(original.name, original.players);
        }

        @Override
        public void add(Player player) {
            throw new UnsupportedOperationException("can't add to an unmodifiable team");
        }

        @Override
        public void remove(Player player) {
            throw new UnsupportedOperationException("can't remove from an unmodifiable team");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("can't clear an unmodifiable team");
        }

        @Override
        public void backup() {
            throw new UnsupportedOperationException("can't backup an unmodifiable team");
        }

        @Override
        public Iterator<Player> iterator() {
            return CollectionUtils.unmodifiableIterator(super.iterator());
        }
    }
}

package dev.directplan.npjobs.job;

import com.google.common.collect.Iterables;
import dev.directplan.npjobs.Tickable;
import dev.directplan.npjobs.keyed.NamespaceKeyed;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author DirectPlan
 */
public interface Job<T extends JobContext> extends Tickable {

    /**
     * Gets a namespaced id for this job
     *
     * @return namespaced id
     */
    NamespaceKeyed getId();

    /**
     * Gets the context of this job.
     *
     * @return the context
     */
    T getContext();

    /**
     * Gets an iterable set of workers allocated to this job.
     *
     * @return iterable set of workers.
     */
    Iterable<Worker> getWorkers();

    /**
     * Adds a worker to the job.
     *
     * @param worker worker to add.
     */
    void addWorker(Worker worker);

    /**
     * Gets the state of this job.
     * <p>
     * <strong>Implemented states:</strong>
     * <ol
     *     <li>{@link State#STARTING}</li>
     *     <li>{@link State#RUNNING}</li>
     *     <li>{@link State#COMPLETED}</li>
     * </ol>
     *
     * @return the state
     */
    State getState();

    /**
     * Sets a new state for this job.
     * <p>
     * A job's current state should by no means return to a previous state.
     * If such thing happens, an {@link IllegalStateException} will be thrown.
     *
     * @param state the new state.
     * @see dev.directplan.npjobs.util.EnumUtils#checkAscendance(Enum, Enum)
     */
    void setState(State state);

    /**
     * Whether this job is able to start.
     * <p>
     * If an exception is caught whilst checking whether this job can start,
     * the exception message should be sent to the {@link Employer} associated with this job.
     *
     * @return whether this job can start.
     */
    boolean canStart();

    /**
     * Fired after {@link Job#canStart()} returns true and the state
     * is set to {@link State#RUNNING}.
     */
    void onStart();

    /**
     * Gets the percentage completion of this job.
     *
     * @return the percentage completion.
     */
    int getCompletion();

    /**
     * Fired as soon as the state is set to {@link State#COMPLETED}
     * @see JobTickUpdater
     */
    void onComplete();

    /**
     * Gets the name of this job.
     *
     * @return name of this job.
     */
    default String getName() {
        return getContext().getName();
    }

    /**
     * Gets the {@link Employer} of this job.
     *
     * @return employer of this job.
     */
    default Employer getEmployer() {
        return getContext().getEmployer();
    }

    /**
     * Gets the amount of workers allocated to this job.
     *
     * @return amount of workers.
     */
    default int getWorkerSize() {
        return Iterables.size(getWorkers());
    }

    enum State {

        STARTING, RUNNING, COMPLETED;

        public boolean isStarting() {
            return this == STARTING;
        }

        public boolean isRunning() {
            return this == RUNNING;
        }

        public boolean isCompleted() {
            return this == COMPLETED;
        }
    }

    interface Employer {

        Employer CONSOLE = asCommandSender(Bukkit.getConsoleSender());

        String getName();

        boolean isOnline();

        boolean isConsole();

        void sendMessage(String message);

        void sendActionBar(String message);

        static Employer asCommandSender(@NotNull CommandSender sender) {
            return new Employer() {

                private CommandSender mutableSender = sender;

                @Override
                public String getName() {
                    return mutableSender.getName();
                }

                @Override
                public boolean isOnline() {
                    if (mutableSender instanceof Player player) return player.isValid();
                    return mutableSender != null;
                }

                @Override
                public boolean isConsole() {
                    return mutableSender instanceof ConsoleCommandSender;
                }

                @Override
                public void sendMessage(String message) {
                    validatePlayer();

                    if (!isOnline()) return;
                    mutableSender.sendMessage(message);
                }

                @Override
                public void sendActionBar(String message) {
                    validatePlayer();
                    if (!isOnline()) return;
                    if (!(mutableSender instanceof Player player)) return;

                    player.spigot()
                            .sendMessage(ChatMessageType.ACTION_BAR,
                                    TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', message)));
                }

                private void validatePlayer() {
                    if (isOnline()) return;

                    Player ghost = (Player) mutableSender;
                    // Keep looking for the player
                    Player player  = Bukkit.getPlayer(ghost.getUniqueId());
                    if (player == null) return;
                    mutableSender = player;
                }
            };
        }
    }
}

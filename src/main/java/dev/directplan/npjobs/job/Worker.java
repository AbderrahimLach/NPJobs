package dev.directplan.npjobs.job;

import dev.directplan.npjobs.keyed.NamespaceKeyed;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a job worker which implements an NPC.
 *
 * @see NPCWorker
 * @author DirectPlan
 */
public interface Worker {

    /**
     * Gets the namespaced id of this worker.
     *
     * @return the namespaced id of this worker.
     */
    NamespaceKeyed getId();

    /**
     * Gets the name of this worker.
     *
     * @return the name of this worker
     */
    String getName();

    /**
     * Gets the location of this worker.
     *
     * @return the location of this worker.
     */
    Location getLocation();

    /**
     * Gets the state of this worker.
     * <p>
     * <strong>Implemented states:</strong>
     * <li>{@link State#NOT_SPAWNED}</li>
     * <li>{@link State#IDLING}</li>
     * <li>{@link State#WORKING}</li>
     * <li>{@link State#FINISHED}</li>
     *
     * @return the state of this worker.
     */
    State getState();

    /**
     * Spawns the worker at the specified {@code location}.
     *
     * @param location where to spawn the worker.
     */
    void spawn(Location location);

    /**
     * Removes this worker from the world they're in. In other words, de-spawns the worker.
     */
    void remove();

    /**
     * Teleports the worker to the specified {@code location}.
     *
     * @param location where to teleport the worker.
     */
    void teleport(Location location);

    /**
     * Walks the worker to the specified {@code location}
     * and whether the worker should be {@code sprinting}.
     *
     * @param location walk destination
     * @param sprinting whether to sprint
     */
    void walkTo(Location location, boolean sprinting);

    /**
     * Sets the worker's held item to the specified {@code item}.
     *
     * @param item to set the worker's held item to.
     */
    void setHeldItem(ItemStack item);

    /**
     * Makes the worker play a swing animation. Preferably main hand.
     */
    void playSwingAnimation();

    /**
     * Makes the worker jump.
     * <p>
     * The jump function is interruptible, which causes the worker to "fly"
     * if triggered repeatedly.
     */
    void jump();

    /**
     * Gets whether the worker is currently jumping.
     *
     * @return whether the worker is currently jumping.
     */
    boolean isJumping();

    /**
     * Gets whether the worker is currently navigating.
     * <p>
     * Returns true if the worker is currently walking a certain path,
     * otherwise false.
     *
     * @return whether the worker is currently navigating.
     */
    boolean isNavigating();

    /**
     * Sets a new state for this worker.
     * <p>
     * A job's current state should by no means return to a previous state.
     * If such thing happens, an {@link IllegalStateException} will be thrown.
     *
     * @param state the new state.
     * @see dev.directplan.npjobs.util.EnumUtils#checkAscendance(Enum, Enum)
     */
    void setState(State state);

    /**
     * Sets whether this worker should be sneaking.
     * <p>
     * Sneaking is only supported for Player NPCs.
     *
     * @param sneaking whether to make the worker sneak.
     */
    default void setSneaking(boolean sneaking) {
        unsupported();
    }

    /**
     * Sets a new skin for the worker.
     *
     * @param skin skin to set.
     */
    default void setSkin(Skin skin) {
        unsupported();
    }

    /**
     * Sets a new skin for the worker from the provided {@code name}.
     * <p>
     * The skin name can be a player's name.
     *
     * @param name a player's name.
     */
    default void setSkin(String name) {
        unsupported();
    }

    /**
     * Self-explanatory.
     */
    private void unsupported() {
        throw new UnsupportedOperationException("This function is not supported");
    }

    enum State {
        NOT_SPAWNED, IDLING, WORKING, FINISHED;

        public boolean isNotSpawned() {
            return this == NOT_SPAWNED;
        }

        public boolean isIdling() {
            return this == IDLING;
        }

        public boolean isWorking() {
            return this == WORKING;
        }

        public boolean isFinished() {
            return this == FINISHED;
        }
    }

    record Skin(String texture, String signature) {}
}

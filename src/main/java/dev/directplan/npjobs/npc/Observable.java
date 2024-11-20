package dev.directplan.npjobs.npc;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author DirectPlan
 */
public interface Observable {

    void addObserver(@NotNull Player player);

    void removeObserver(@NotNull Player player);
}

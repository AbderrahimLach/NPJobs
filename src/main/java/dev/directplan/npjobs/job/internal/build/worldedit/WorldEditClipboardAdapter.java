package dev.directplan.npjobs.job.internal.build.worldedit;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.session.ClipboardHolder;
import dev.directplan.npjobs.job.internal.build.Clipboard;
import dev.directplan.npjobs.job.internal.build.ClipboardAdapter;
import dev.directplan.npjobs.job.internal.build.ClipboardContext;
import dev.directplan.npjobs.job.internal.build.ClipboardException;
import org.bukkit.entity.Player;

/**
 * @author DirectPlan
 */
public final class WorldEditClipboardAdapter implements ClipboardAdapter {

    private static final WorldEdit WORLD_EDIT = WorldEdit.getInstance();

    @Override
    public Clipboard select(Player player, ClipboardContext context) throws ClipboardException {
        LocalSession localSession = WORLD_EDIT.getSessionManager().get(BukkitAdapter.adapt(player));
        try {
            ClipboardHolder clipboardHolder = localSession.getClipboard();
            return new WorldEditClipboard(clipboardHolder.getClipboard(), context);
        } catch (EmptyClipboardException e) {
            throw new ClipboardException("Clipboard is empty. Use //copy or load a schematic.");
        }
    }
}

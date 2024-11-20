package dev.directplan.npjobs.job.internal.build;

import dev.directplan.npjobs.job.Job;
import dev.directplan.npjobs.job.JobManager;
import dev.directplan.npjobs.job.Jobs;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.YELLOW;

/**
 * @author DirectPlan
 */
public class PasteBuildCommand extends Command {

    private final JobManager jobManager;

    public PasteBuildCommand(JobManager jobManager) {
        super("pastebuild");
        this.jobManager = jobManager;

        setPermission("npjobs.job.build");
        setUsage("/pastebuild <name> <worker size> [<world> <firstPos (x,y,z)> <secondPos (x,y,z)> <pastePos (x,y,z)>]");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender,
                           @NotNull String s,
                           @NotNull String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

        String numStr = args[1];
        if (!NumberUtils.isNumber(numStr)) {
            sender.sendMessage(RED + "'" + numStr + "' is not a number.");
            return true;
        }
        int maxWorkers = Integer.parseInt(numStr);
        if (args.length < 5) {
            if (!(sender instanceof Player player)) {
                sendUsage(sender);
                return true;
            }

            jobManager.startJob(Jobs.BUILD, BuildJob.Context.builder()
                    .name(args[0])
                    .maxWorkers(maxWorkers)
                    .pasteLocation(player.getLocation())
                    .employer(Job.Employer.asCommandSender(player))
                    .clipboardType(ClipboardType.copy(player, ClipboardContext.DEFAULT))
                    .build());
            broadcastJobMessage(sender, args[0]);
            return true;
        }

        World world = Bukkit.getWorld(args[2]);
        if (world == null) {
            sender.sendMessage(RED + "World '" + args[2] + "' does not exist.");
            return true;
        }

        Vector firstPos = toVector(args[3]);
        Vector secondPos = toVector(args[4]);
        Location pasteLocation = toVector(args[5]).toLocation(world);

        broadcastJobMessage(sender, args[0]);
        Selection selection = Selection.from(world, firstPos, secondPos);
        jobManager.startJob(Jobs.BUILD, BuildJob.Context.builder()
                .name(args[0])
                .maxWorkers(maxWorkers)
                .pasteLocation(pasteLocation)
                .clipboardType(ClipboardType.copy(selection, ClipboardContext.DEFAULT))
                .build());
        return true;
    }

    private void broadcastJobMessage(CommandSender sender, String name) {
        Command.broadcastCommandMessage(sender,
                GREEN + "Starting build job " + YELLOW + name + GREEN + "...",
                false);
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(RED + usageMessage);
    }
    private Vector toVector(String pos) {
        String[] coordinates = pos.split(",");
        return new Vector(
                Double.parseDouble(coordinates[0]),
                Double.parseDouble(coordinates[1]),
                Double.parseDouble(coordinates[2])
        );
    }
}

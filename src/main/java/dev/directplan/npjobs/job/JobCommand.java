package dev.directplan.npjobs.job;

import com.google.common.collect.Streams;
import dev.directplan.npjobs.keyed.Keyed;
import org.bukkit.ChatColor;
import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.YELLOW;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author DirectPlan
 */
public class JobCommand extends Command {

    private final JobManager jobManager;

    public JobCommand(JobManager jobManager) {
        super("job");
        this.jobManager = jobManager;

        setPermission("npjobs.job.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender,
                             @NotNull String s,
                             @NotNull String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list": {
                sender.sendMessage(YELLOW.toString() + BOLD + "Current Jobs:");
                int order = 1;
                for (Job<?> job : jobManager.getActiveJobs()) {
                    String name = job.getName();
                    String id = job.getId().key();
                    Job.Employer employer = job.getEmployer();

                    sender.sendMessage(GOLD + " " + order + ". " + name + GRAY + " (" + id + ")");
                    sender.sendMessage(YELLOW + "     Status: " + GOLD + job.getState());
                    sender.sendMessage(YELLOW + "     Workers: " + GOLD + job.getWorkerSize());
                    sender.sendMessage(YELLOW + "     Employer: " + GOLD + employer.getName());
                    order++;
                }
                return true;
            }
            case "status": {
                if (args.length < 2) {
                    sender.sendMessage(RED + "Usage: /job status <job id> (Shows status of a job)");
                    return true;
                }
                Job<?> job = jobManager.getActiveJob(Keyed.simple(args[1])).orElse(null);
                if (job == null) {
                    sender.sendMessage(RED + "There is no active job by the name: " + args[1]);
                    return true;
                }
                String id = job.getId().key();
                Job.Employer employer = job.getEmployer();
                String employerStatus = employer.isOnline() ? GREEN + "Online" : RED + "Offline";
                Iterable<Worker> workers = job.getWorkers();
                int workersSize = job.getWorkerSize();

                sender.sendMessage(YELLOW.toString() + BOLD + "Job status of " + job.getName() + GRAY + " (" + id + ")");
                sender.sendMessage(GRAY + " * " + YELLOW + "State: " + GOLD + job.getState());
                sender.sendMessage(GRAY + " * " + YELLOW + "Employer: " + GOLD + employer.getName() + GRAY + " (" + employerStatus + ")");
                sender.sendMessage(GRAY + " * " + YELLOW + "Completion: " + GOLD + job.getCompletion() + '%');
                sender.sendMessage(GRAY + " * " + YELLOW + "Workers " + GOLD + "(" + workersSize + "):");

                Streams.stream(workers)
                        .limit(5)
                        .forEach(worker -> sender.sendMessage(
                                GRAY + "   * " + YELLOW + "Name: " + GOLD + worker.getName() +
                                        YELLOW + " State: " + GOLD + worker.getState()));
                if (workersSize > 5) sender.sendMessage(GRAY + "     (and " + (workersSize - 5) + " more workers...)");

                return true;
            }
            case "complete":
            case "stop": {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /job stop <job id> (Forcefully stops a job)");
                    return true;
                }
                Job<?> job = jobManager.getActiveJob(Keyed.simple(args[1])).orElse(null);
                if (job == null) {
                    sender.sendMessage(RED + "There is no active job by the name: " + args[1]);
                    return true;
                }
                sender.sendMessage(YELLOW + "Forcefully completing " + job.getName() + " job...");
                jobManager.completeJob(job);
                sender.sendMessage(GREEN + "You have forcefully completed " + YELLOW + job.getName() + GREEN + " job.");
                return true;
            }
            // TODO: Add start sub command with job context parser support
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage:");
        sender.sendMessage(ChatColor.RED + "- /job list (Displays active jobs)");
        sender.sendMessage(ChatColor.RED + "- /job status <job id> (Shows status of the specified job)");
        sender.sendMessage(ChatColor.RED + "- /job stop,complete <job id> (Forcefully stops the specified job)");
    }
}

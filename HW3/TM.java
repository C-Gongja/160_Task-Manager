import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.text.SimpleDateFormat;
import java.time.*;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.function.Function;

/* ******** CHECK LIST ********
 * description in quotation
 * Some edge cases
 */

// Main Program
public class TM {
	public static void main(String[] args) {

		final String logfile = "TM.txt";

		if (args.length < 1) {
			System.out.println("Usage: java TM.java <command> <data>");
			return;
		}

		final String cmd = args[0].toUpperCase();
		Map<String, TaskInfo> tasks = LogParser.parse(logfile);
		
		try {
			TaskCommands taskCommand = TaskCommands.valueOf(cmd);
			TaskStrategy ts = new TaskStrategy();
			ts.setStrategy(taskCommand.getCommandStrategy());
			ts.executeCommand(args, tasks);
		} catch (IllegalArgumentException e) {
			System.out.println("\nInvalid command: " + cmd);
		}
	}
}

enum TaskCommands {
	START(new StartCommand()),
	STOP(new StopCommand()),
	DELETE(new DeleteCommand()),
	DESCRIBE(new DescribeCommand()),
	SUMMARY(new SummaryCommand()),
	SIZE(new SizeCommand()),
	RENAME(new RenameCommand());

	private final CommandStrategy commandStrategy;

	private TaskCommands(CommandStrategy commend) {
		this.commandStrategy = commend;
	}

	public CommandStrategy getCommandStrategy() {
		return this.commandStrategy;
	}
}

enum TaskSizes {
	S, M, L, XL, 
}

class TaskInfo {
	public final String taskName;
	public final String command;
	public final String description;
	public final String startTime;
	public final String stopTime;
	public final String totalTime;
	public final TaskSizes size;

	public TaskInfo( String taskName, String command, 
						String description, TaskSizes size, 
							String startTime, String stopTime, 
												String totalTime) {
		this.taskName = taskName;
		this.command = command;
		this.description = description;
		this.size = size;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.totalTime = totalTime;
	}

	@Override
	public String toString() {
		return "\nTask name: " + taskName + '\n' +
				"Description: " + description + '\n' +
				"Size: " + size + '\n' +
				"Total time spent: " + totalTime;
	}
}

// Strategy Pattern
interface CommandStrategy {
	void execute(String[] arg, Map<String, TaskInfo> tasks);
}

class StartCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg, Map<String, TaskInfo> tasks) {
		boolean anyOngoingTask = tasks.values().stream()
																.anyMatch(task -> task.command
																.equals("start"));

		if (!anyOngoingTask) {
		final String line = arg[1] + '\t' + arg[0];
		Util.writeLog(line);
		} else {
		System.out.println("Cannot start a new task. There are ongoing tasks.");
		}
	}
}
class StopCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg, Map<String, TaskInfo> tasks) {
		boolean anyOngoingTask = tasks.values().stream()
																.anyMatch(task -> task.command
																.equals("stop") || 
																task.command.equals("delete"));

		if (!anyOngoingTask) {
		final String line = arg[1] + '\t' + arg[0];
		Util.writeLog(line);
		} else {
		System.out.println("Cannot stop the task. Task is not ongoing.");
		}
	}
}
class DeleteCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg, Map<String, TaskInfo> tasks) {
		boolean anyOngoingTask = tasks.values().stream()
																.anyMatch(task -> task.command
																.equals("delete"));

		if (!anyOngoingTask) {
		final String line = arg[1] + '\t' + arg[0];
		Util.writeLog(line);
		} else {
		System.out.println("It's already deleted.");
		}
	}
}

class DescribeCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg, Map<String, TaskInfo> tasks) {
		final String description = String.join(" ", Arrays.copyOfRange(arg, 2, arg.length));
		//check optional [size] command
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ isSizeIncluded(description);
		Util.writeLog(line);
	}

	private String isSizeIncluded(String str) {
		String trimmed = str.substring(str.lastIndexOf(" ") + 1).toUpperCase();
		if (Arrays.stream(TaskSizes.values()).anyMatch(size -> size.name().equals(trimmed))) {
			String line = str.replace(trimmed, "") + '\t' + trimmed;
			return line;
		}
		return str;
	}
}

class SummaryCommand implements CommandStrategy {

	@Override
	public void execute(String[] arg, Map<String, TaskInfo> tasks) {
		if (arg.length < 2) {
			summaryTotal(tasks);
		}
		else if (Arrays.stream(TaskSizes.values()).anyMatch(size -> size.name().equals(arg[1].toUpperCase()))){
			summaryBySize(arg[1], tasks);
		}
		else {
			summaryByTask(arg[1], tasks);
		}
	}

	private static void summaryBySize(String size, Map<String, TaskInfo> tasks) {
		 for (TaskInfo taskInfo : tasks.values()) {
			if (taskInfo.size == TaskSizes.valueOf(size.toUpperCase())) {
				System.out.println(taskInfo);
			}
			else {
				System.out.println("taks with size "+size+ " does not exists");
			}
		}
	}

	private static  void summaryByTask(String taskName, Map<String, TaskInfo> tasks) {
		for (TaskInfo taskInfo : tasks.values()) {
			if (taskName.equals(taskInfo.taskName)) {
				System.out.println(taskInfo);
			}
			else {
				System.out.println(taskName + " does not exists");
			}
		}
	}

	//add total time spent on all task 
	private static  void summaryTotal(Map<String, TaskInfo> tasks) {
		for (TaskInfo taskInfo : tasks.values()) {
			System.out.println(taskInfo);
		}
	}
}
class RenameCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg, Map<String, TaskInfo> tasks) {
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ arg[2];
		Util.writeLog(line);
	}
}
class SizeCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg, Map<String, TaskInfo> tasks) {
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ arg[2];
		Util.writeLog(line);
	}
}
class TaskStrategy {
	private CommandStrategy currentStrategy;

	public void setStrategy(CommandStrategy strategy) {
		this.currentStrategy = strategy;
	}

	public void executeCommand(String[] args, Map<String, TaskInfo> tasks) {
		currentStrategy.execute(args, tasks);
	}
}

class LogParser {
	public static Map<String, TaskInfo> parse(String logfile) {
		try {
			return Files.lines(Path.of(logfile))
					.map(LogParser::parseLogEntry)
					.collect(Collectors.toMap( 
						taskInfo -> taskInfo.taskName,
						Function.identity(),
						(storedInfo, newInfo) -> {
							if (newInfo.command.equals("delete")) {
								return null;
							} else {
								return new TaskInfo(
									storedInfo.taskName,
									!newInfo.command.isEmpty() ? newInfo.command : storedInfo.command,
									!newInfo.description.isEmpty() ? storedInfo.description + '\n' + newInfo.description : storedInfo.description,
									newInfo.size != null ? newInfo.size : storedInfo.size,
									!newInfo.startTime.isEmpty() ? newInfo.startTime : storedInfo.startTime,
									!newInfo.stopTime.isEmpty() ? newInfo.stopTime : storedInfo.stopTime,
									calculateTotalTime(storedInfo.startTime, newInfo.stopTime, storedInfo.totalTime)
								);
							}
						}
					))
					.entrySet().stream()
					.filter(entry -> entry.getValue() != null)
					.collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue));
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading log file", e);
		}
	}

	private static TaskInfo parseLogEntry(String line) {
		final String[] parts = line.split("\t");

		final String startTime = parts[0];
		final String taskName = parts[1];
		final String command = parts[2];
		String description = "";
		TaskSizes size = null;

		if ("size".equals(command)) {
			size = TaskSizes.valueOf(parts[3]);
		} else if ("describe".equals(command)) {
			description = (parts.length > 3) ? parts[3] : "";
			size = (parts.length > 4) ? TaskSizes.valueOf(parts[4]) : null;
		}

		final String stopTime = "stop".equals(command) ? startTime : "";
		final String totalTime = "00:00:00";

		return new TaskInfo(taskName, command, description, size, startTime, stopTime, totalTime);
	}

	private static String calculateTotalTime(
						String startTime, String stopTime, String totalTime) {

		if (!startTime.isEmpty() && !stopTime.isEmpty()) {
			final LocalDateTime startDateTime = LocalDateTime.parse(startTime);
			final LocalDateTime stopDateTime = LocalDateTime.parse(stopTime);
			final Duration duration = Duration.between(startDateTime, stopDateTime);

			LocalTime totalDuration = LocalTime.parse(totalTime);
			totalDuration = totalDuration.plusHours(duration.toHours());
			totalDuration = totalDuration.plusMinutes(duration.toMinutesPart());
			totalDuration = totalDuration.plusSeconds(duration.toSecondsPart());
	
			return totalDuration.toString();
		}
		return totalTime;
	}
}

class Util {
	private static final String logfile = "TM.txt";
	private static final String TIME_FORMAT= "yyyy-MM-dd'T'HH:mm:ss";

	public static void writeLog(String line) {
		final String timeStamp = new SimpleDateFormat(TIME_FORMAT).format(new Date());
		final String outputLine = timeStamp +'\t'+ line;
		try{
			FileWriter fw=new FileWriter(logfile, true);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(outputLine);
			bw.newLine();
			bw.flush();
			bw.close();
			fw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

public class TM {
	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Usage: java TM.java <command> <data>");
			return;
		}

		final String cmd = args[0].toUpperCase();
		
		try {
			TaskCommands taskCommand = TaskCommands.valueOf(cmd);
			TaskStrategy ts = new TaskStrategy();
			ts.setStrategy(taskCommand.getCommandStrategy());
			ts.executeCommand(args);
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
	private TaskCommands(CommandStrategy cmd) {this.commandStrategy = cmd;}

	public CommandStrategy getCommandStrategy() {return this.commandStrategy;}
}

enum TaskSizes {
	S, M, L, XL, NONE 
}

// Strategy Pattern
interface CommandStrategy {
	void execute(String[] arg);
}

class StartCommand implements CommandStrategy {
	private static final UserLogger userLogger = UserLogger.getInstance();

	@Override
	public final void execute(String[] arg) {
		if(!hasOngoingTask(userLogger.getTasks())) {
			final String line = arg[1] + '\t' + arg[0];
			UserLogger.writeLog(line);
		} else {
			System.out.println("Cannot start a new task. There are ongoing tasks.");
		}
	}
	
	private boolean hasOngoingTask(List<TaskInfo> tasks) {
		return tasks.stream()
								.anyMatch(task -> task.command.equals("START"));
	}
}

class StopCommand implements CommandStrategy {
	private static final UserLogger userLogger = UserLogger.getInstance();

	@Override
	public final void execute(String[] arg) {
		final String taskName = arg[1];
		TaskInfo targetTaskInfo = userLogger.isContain(taskName);
		if (targetTaskInfo.command.equals("START")) {
			final String line = arg[1] + '\t' + arg[0];
			UserLogger.writeLog(line);
		} else {
			System.out.println("Cannot stop the task. Task is not ongoing.");
		}
	}
}

class DeleteCommand implements CommandStrategy {
	private static final UserLogger userLogger = UserLogger.getInstance();

	@Override
	public final void execute(String[] arg) {
		if (doesTaskExists(userLogger.getTasks(), arg[1])) {
			final String line = arg[1] + '\t' + arg[0];
			UserLogger.writeLog(line);
		} else {
			System.out.println("It's not on the task manager.");
		}
	}

	private boolean doesTaskExists(List<TaskInfo> tasks, String taskName) {
		return tasks.stream()
								.anyMatch(task -> task.taskName.equals(taskName));
	}
}

class DescribeCommand implements CommandStrategy {
	@Override
	public final void execute(String[] arg) {
		final String description = String.join(" ", Arrays.copyOfRange(arg, 2, arg.length));

		//check optional [size] command
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ isSizeIncluded(description);
		UserLogger.writeLog(line);
	}

	private static String isSizeIncluded(String str) {
		String trimmed = str.substring(str.lastIndexOf(" ") + 1).toUpperCase();
		if (Arrays.stream(TaskSizes.values()).anyMatch(size -> size.name().equals(trimmed))) {
			String line = str.replace(trimmed, "") + '\t' + trimmed;
			return line;
		}
		return str;
	}
}

class SummaryCommand implements CommandStrategy {
	private static final UserLogger userLogger = UserLogger.getInstance();

	@Override
	public final void execute(String[] arg) {
		if (arg.length > 2) {
			throw new IllegalArgumentException("Invalid summary format");
		}
		var tasks = userLogger.getTasks();
		if (arg.length < 2) {
			summaryTotal(tasks);
		} else {
			String keyword = arg[1];
			if (Arrays.stream(TaskSizes.values())
								.anyMatch(size -> size.name()
								.equals(keyword.toUpperCase()))) {
												summaryBySize(keyword.toUpperCase(), tasks);
			} else {
					summaryByTask(keyword, tasks);
			}
		}
	}

	private static void summaryBySize(String size, List<TaskInfo>	tasks) {
		TaskSizes targetSize = TaskSizes.valueOf(size);
		List<TaskInfo> matchingTasks = tasks.stream()
						.filter(task -> task.size.equals(targetSize))
						.collect(Collectors.toList());

		if (!matchingTasks.isEmpty()) {
			System.out.println("\n----------------------------");
			System.out.println("TASK SIZE " + size + " SUMMARY");
			System.out.println("----------------------------");
			matchingTasks.forEach(task -> {
				System.out.println(task);
				System.out.println("----------------------------");
			});
		} else {
			System.out.println("No tasks found with size: " + size);
		}
	}

	private static void summaryByTask(String taskName, List<TaskInfo> tasks) {
		boolean tasksFound = tasks.stream()
						.filter(task -> task.taskName.equals(taskName))
						.peek(task -> {
							System.out.println("\n----------------------------");
							System.out.println(taskName + " TASK SUMMARY");
							System.out.println("----------------------------");
							System.out.println(task);
							System.out.println("----------------------------");
						}).findAny().isPresent();
		if (!tasksFound) {
			System.out.println("No task found: " + taskName);
		}
	}

	private static void summaryTotal(List<TaskInfo> tasks) {
		System.out.println("\n----------------------------");
		System.out.println("TOTAL TASKS SUMMARY");
		tasks.forEach(task -> {
			System.out.println("----------------------------");
			System.out.println(task);
		});
		summaryStatistic(tasks);
		printTotalTime(tasks);
	}
	
	private static void printTotalTime(List<TaskInfo> tasks) {
		System.out.println("----------------------------");
		System.out.println("\nTotal time spent on all tasks: " + 
							timeFormat(tasks.stream()
							.mapToLong(task -> taskDuration(task))
							.sum()));
		System.out.println();
	}

	private static void summaryStatistic(List<TaskInfo> tasks) {
		Map<TaskSizes, List<TaskInfo>> tasksBySize = tasks.stream()
						.filter(task -> task.size != TaskSizes.NONE)
						.collect(Collectors.groupingBy(TaskInfo::getSize));

		System.out.println("\n----------------------------");
		System.out.println("STATISTIC SUMMARY");

		tasksBySize.forEach((size, sizeTasks) -> {
			if (sizeTasks.size() > 1) {
				System.out.println("----------------------------");
				System.out.println("\nSize: " + size);

				DoubleSummaryStatistics summary = calcDuration(sizeTasks);
				System.out.println("Min Time: "+timeFormat((long)summary.getMin()));
				System.out.println("Max Time: "+timeFormat((long)summary.getMax()));
				System.out.println("Avg Time: "+timeFormat((long)summary.getAverage()));
			}
		});
	}

	private static DoubleSummaryStatistics calcDuration(List<TaskInfo> tasks) {
		return tasks.stream()
								.mapToDouble(task -> taskDuration(task))
								.summaryStatistics();
	}

	private static long taskDuration(TaskInfo task) {
		String[] timeParts = task.totalTime.split(":");
		int hours = Integer.parseInt(timeParts[0]);
		int minutes = Integer.parseInt(timeParts[1]);
		int seconds = Integer.parseInt(timeParts[2]);

		return hours * 3600 + minutes * 60 + seconds;
	}

	private static String timeFormat(long durationInSeconds) {
		long hours = durationInSeconds / 3600;
		long minutes = (durationInSeconds % 3600) / 60;
		long seconds = durationInSeconds % 60;

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}

class RenameCommand implements CommandStrategy {
	@Override
	public final void execute(String[] arg) {
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ arg[2];
		UserLogger.writeLog(line);
	}
}
class SizeCommand implements CommandStrategy {
	@Override
	public final void execute(String[] arg) {
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ arg[2];
		UserLogger.writeLog(line);
	}
}
class TaskStrategy {
	private CommandStrategy currentStrategy;

	public void setStrategy(CommandStrategy strategy) {
		this.currentStrategy = strategy;
	}

	public void executeCommand(String[] args) {
		currentStrategy.execute(args);
	}
}

class UserLogger {
	private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String logfile = "TM.txt";
	private List<TaskInfo> tasks;
	
	private static final UserLogger instance = new UserLogger();

	private UserLogger() {
			this.tasks = LogParser.parse(logfile);
	}

	public static UserLogger getInstance() {
			return instance;
	}

	public List<TaskInfo> getTasks() {
			return tasks;
	}

	public TaskInfo isContain(String key) {
		return tasks.stream()
								.filter(task -> task.taskName.equals(key))
								.findFirst().orElse(null);
	}

	public static void writeLog(String line) {
		final String timeStamp = new SimpleDateFormat(TIME_FORMAT).format(new Date());
		final String outputLine = timeStamp + '\t' + line;
		try {
				FileWriter fw = new FileWriter(logfile, true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(outputLine);
				bw.newLine();
				bw.flush();
				bw.close();
				fw.close();
		} catch (Exception e) {
			System.err.println("Error: making logs.");
			System.exit(1);
		}
	}
}

class TaskInfo {
	public final String taskName;
	public final String command;
	public final String description;
	public final String startTime;
	public final String stopTime;
	public final String totalTime;
	public final TaskSizes size;
	public final String prevName;
	
	public TaskInfo( String taskName, String command, 
														String description, TaskSizes size, 
																	String startTime, String stopTime, 
																				String totalTime, String prevName) 
	{
		this.taskName = taskName;
		this.command = command;
		this.description = description;
		this.size = size;
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.totalTime = totalTime;
		this.prevName = prevName;
	}
	
	public String getTaskName() {
		return this.taskName;
	}
	public String getCommand() {
		return this.command;
	}
	public TaskSizes getSize() {
		return this.size;
	}
	@Override
	public String toString() {
		return "\nTask name: " + taskName + '\n' +
				"Description: " + description + '\n' +
				"Size: " + size + '\n' +
				"Total time spent: " + totalTime;
	}
}

class LogParser {
	private static List<String> readLogFile(String logfile) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(logfile))) {
			String line;
			while ((line = br.readLine()) != null) {
					lines.add(line);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error: The log is not exists.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error reading log file '" + logfile + "'.");
			System.exit(1);
		}
		return lines;
	}

	public static List<TaskInfo> parse(String logfile) {
		List<TaskInfo> tasks = new ArrayList<>();
		List<String> lines = readLogFile(logfile);

		for (String line : lines) {
			TaskInfo newInfo = parseLogEntry(line);
			if(newInfo.command.equals("DELETE")) {
				TaskInfo existingInfo = findTaskByName(tasks, newInfo.taskName);
				tasks.remove(existingInfo);
			} else if(newInfo.command.equals("RENAME")) {
				TaskInfo existingInfo = findTaskByName(tasks, newInfo.prevName);
				TaskInfo updatedInfo = updateTaskInfo(existingInfo, newInfo);
				tasks.remove(existingInfo);
				tasks.add(updatedInfo);
			}
			else {
				TaskInfo existingInfo = findTaskByName(tasks, newInfo.taskName);
				TaskInfo updatedInfo = updateTaskInfo(existingInfo, newInfo);
				tasks.remove(existingInfo);
				tasks.add(updatedInfo);
			}
		}
		return tasks;
	}

	private static TaskInfo findTaskByName(List<TaskInfo> tasks,String taskName){
		return tasks.stream().filter(task -> task.taskName.equals(taskName))
								.findFirst().orElse(null);
	}

	private static TaskInfo parseLogEntry(String line) {
		final String[] parts = line.split("\t");

		final String time = parts[0];
		String taskName = parts[1];
		final String command = parts[2].toUpperCase();
		String description = "";
		TaskSizes size = TaskSizes.NONE;
		String totalTime = "00:00:00";
		String prevName = "";

		if (command.equals("SIZE")) {
			size = TaskSizes.valueOf(parts[3]);
		} else if (command.equals("DESCRIBE")) {
			description = (parts.length > 3) ? parts[3] : "";
			size = (parts.length > 4) ? TaskSizes.valueOf(parts[4]) : TaskSizes.NONE;
		} else if (command.equals("RENAME")) {
			prevName = taskName;
			taskName = parts[3];
		}

		final String startTime = command.equals("START") ? time : "";
		final String stopTime = command.equals("STOP") ? time : "";
		totalTime = calculateTotalTime(startTime, startTime, totalTime);

	return new TaskInfo(taskName, command, description, size, 
																		startTime, stopTime, totalTime, prevName);
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

	private static TaskInfo updateTaskInfo(TaskInfo storedInfo,TaskInfo newInfo){
		if (storedInfo == null) {
			return newInfo;
	}

	final String updatedTaskName = (newInfo.command.equals("RENAME"))
		? newInfo.taskName : storedInfo.taskName;

	final String updatedCommand = 
		(newInfo.command.equals("START") || newInfo.command.equals("STOP"))
			? newInfo.command : storedInfo.command;

	final String updatedDescription = 
		(newInfo.description != null && !newInfo.description.isEmpty())
			? storedInfo.description + '\n' + newInfo.description 
			: storedInfo.description;

	final TaskSizes updatedSize = 
		(newInfo.size != null && newInfo.size != TaskSizes.NONE)
			? newInfo.size : storedInfo.size;

	final String updatedStartTime = 
		(newInfo.startTime != null && !newInfo.startTime.isEmpty())
			? newInfo.startTime : storedInfo.startTime;

	final String updatedStopTime = 
		(newInfo.stopTime != null && !newInfo.stopTime.isEmpty())
			? newInfo.stopTime : storedInfo.stopTime;

	final String updatedTotalTime = calculateTotalTime(
		storedInfo.startTime, newInfo.stopTime, storedInfo.totalTime);
	
	final String updatedPrevName = 
		(newInfo.stopTime != null && !newInfo.stopTime.isEmpty())
			? newInfo.stopTime : storedInfo.stopTime;

	return new TaskInfo(
		updatedTaskName,
		updatedCommand,
		updatedDescription,
		updatedSize,
		updatedStartTime,
		updatedStopTime,
		updatedTotalTime,
		updatedPrevName);
	}
}

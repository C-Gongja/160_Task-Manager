import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.*;

import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.function.Function;
import java.io.IOException;
import java.io.UncheckedIOException;

/*
 * description in quotation
 */

// Main Program
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

/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * State pattern for start, stop, and delete ?? 
 * This can't be remove if we use TaskInfo class to store
 * each task information as an instance
 * because we can check choosen task states (wheter it is in start, stop, or deelete)
 * 
 */
interface TaskState {
    TaskState start();
    TaskState stop();
    TaskState delete();
	
    String toString();
}


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
class TaskInfo {
	private String taskName;
	private String command;
	private String description;
	private String startTime;
	private String stopTime;
	private String totalTime;
	private TaskSizes size;

	public TaskInfo(String taskName) {
		this.taskName = taskName;

		//so parsing here??
	}

	private String parsing() {
		return null;
	}

	String getString() {
		return null;
	}
}



// Strategy Pattern
interface CommandStrategy {
	void execute(String[] arg);
}

//make start, stop, and delete to state pattern
class StartCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		final String line = arg[1] +'\t'+ arg[0];
		Util.writeLog(line);
	}
}
class StopCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		final String line = arg[1] +'\t'+ arg[0];
		Util.writeLog(line);
	}
}
class DeleteCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		final String line = arg[1] +'\t'+ arg[0];
		Util.writeLog(line);
	}
}

//should I seperate UP and DOWN commands??????????????????????????????????????????????????????//
//OR is it unneccesary??

class DescribeCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		final String description = String.join(" ", Arrays.copyOfRange(arg, 2, arg.length));
		//check optional [size] command
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ isSizeIncluded(description);
		Util.writeLog(line);
	}

	//what if I add another command to describe?
	private String isSizeIncluded(String str) {
		String trimmed = str.substring(str.lastIndexOf(" ") + 1).toUpperCase();
		if (Arrays.stream(TaskSizes.values()).anyMatch(size -> size.name().equals(trimmed))) {
			String line = str.replace(trimmed, "") + '\t' + trimmed;
			return line;
		}
		return str;
	}
}
/*
 * stream can not be reuse after it has already been consumed or closed.!!!
 */
class SummaryCommand implements CommandStrategy {
	private static Stream<TaskStream> taskInfoMap = ParsingLog.readLog();

	@Override
	public void execute(String[] arg) {
		if (arg.length < 2) {
			summaryTotal();
		}
		else if (Arrays.stream(TaskSizes.values()).anyMatch(size -> size.name().equals(arg[1]))){
			summaryBySize(arg[1]);
		}
		else {
			summaryByTask(arg[1]);
		}
	}

	private static void summaryBySize(String size) {
		var taksofsizeList = TaskProc.getTaskOfSize(size, taskInfoMap);
		taksofsizeList.forEach(taskList -> {
			System.out.println('\n'+ taskList.toString());
		});
	}

	private static  void summaryByTask(String taskName) {
		var nameList = TaskProc.getTaskbyName(taskName, taskInfoMap);
		nameList.forEach(taskList -> {
			System.out.println('\n'+ taskList.toString());
		});
	}

	//total time spent on all task 
	private static  void summaryTotal() {
		taskInfoMap.forEach(taskList -> {
			System.out.println('\n'+ taskList.toString());
		});
	}
}
class RenameCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		//Observer update
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ arg[2];
		Util.writeLog(line);
	}
}
class SizeCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		//Observer update
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ arg[2];
		Util.writeLog(line);
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

// Using Stream ????
record TaskStream(String taskName, String command, String description, String size, String startTime, String stopTime, String totalTime) {

	@Override
	public String toString() {
		return  "Task name: " + taskName + '\n' +
				"Description: " + description + '\n' +
				"Size: " + size + '\n' +
				"Total time spent: " + totalTime;
	}
}

class TaskProc {
	public static Stream<TaskStream> getTaskbyName(String name, Stream<TaskStream> taskInfos) {
		return taskInfos
			.filter(task -> task.taskName().equals(name));
	}
	public static Stream<TaskStream> getTaskOfSize(String size, Stream<TaskStream> taskInfos) {
		return taskInfos
			.filter(task -> task.size().equals(size));
	}
}

/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * need to refactor
 * maybe use Task class that stores each task information
 * make a class to store each task information as an instance?
 */
class ParsingLog {
	private static final String logfile = "TM.txt";

	// how can I check the log file format while I parsing the file
	public static Stream<TaskStream> readLog() {
		try {
			return Files.lines(Path.of(logfile))
					.map(ParsingLog::parseLogLine)
					.collect(Collectors.toMap( TaskStream::taskName, Function.identity(),
						(storedInfo, newInfo) -> {
							if (newInfo.command().equals("delete")) {
								return null;
							} else {
								return new TaskStream(
									storedInfo.taskName(),
									!newInfo.command().isEmpty() ? newInfo.command() : storedInfo.command(),
									!newInfo.description().isEmpty() ? storedInfo.description() + '\n' + newInfo.description() : storedInfo.description(),
									newInfo.size().isEmpty() ? storedInfo.size() : newInfo.size(),
									!newInfo.startTime().isEmpty() ? newInfo.startTime() : storedInfo.startTime(),
									!newInfo.stopTime().isEmpty() ? newInfo.stopTime() : storedInfo.stopTime(),
									calculateTotalTime(storedInfo.startTime(), newInfo.stopTime(), storedInfo.totalTime())
								);
							}
						}
					))
					.values().stream()
					.filter(taskStream -> taskStream != null);
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading log file", e);
		}
	}

	private static TaskStream parseLogLine(String line) {
		final String[] parts = line.split("\t");

		final String startTime = parts[0];
		final String taskName = parts[1];
		final String command = parts[2];
		String description = "";
		String size = "";

		// descriptions are inside quotation marks
		if ("size".equals(command)) {
			size = parts[3];
		} else if ("describe".equals(command)) {
			description = (parts.length > 3) ? parts[3] : "";
			size = (parts.length > 4) ? parts[4] : "";
		}

		final String stopTime = "stop".equals(command) ? startTime : "";
		final String totalTime = "00:00:00";

		return new TaskStream(taskName, "", description, size, startTime, stopTime, totalTime);
	}

	private static String calculateTotalTime(String startTime, String stopTime, String totalTime) {
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

// Observer pattern? update
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

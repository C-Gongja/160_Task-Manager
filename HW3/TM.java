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
import java.util.Map;

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
		final String cmd = args[0];
		TaskStrategy ts = new TaskStrategy();

		// Set initial state or strategy
		switch (cmd) {
			case "start":
				ts.setStrategy(new StartCommand());
				break;
			case "stop":
				ts.setStrategy(new StopCommand());
				break;
			case "describe":
				ts.setStrategy(new DescribeCommand());
				break;
			case "summary":
				ts.setStrategy(new SummaryCommand());
				break;
			case "rename":
				ts.setStrategy(new RenameCommand());
				break;
			case "size":
				ts.setStrategy(new SizeCommand());
				break;
			default:
				throw new IllegalArgumentException("\n Invalid command: " + cmd);
		}
		ts.executeCommand(args);
	}
}

class Tasks {
	private static Tasks logger;
	private Map<String, TaskStream> taskInfoMap;

	private Tasks() {

	}

	public static Tasks getInstance() {
        if(logger == null) {
			logger = new Tasks();
		}
		return logger;
    }

	public void deleteTask() {

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
		//Observer update
		final String line = arg[1] +'\t'+ arg[0];
		Util.addLog(line);
	}
}
class StopCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		//Observer update
		final String line = arg[1] +'\t'+ arg[0];
		Util.addLog(line);
	}
}
class DeleteCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		//Observer update
		final String line = arg[1] +'\t'+ arg[0];
		Util.addLog(line);
	}
}
class DescribeCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		//Observer update
		final String description = String.join(" ", Arrays.copyOfRange(arg, 2, arg.length));
		//check optional [size] command
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ isSizeIncluded(description);
		Util.addLog(line);
	}

	//what if I add another command to describe?
	private String isSizeIncluded(String str) {
		String[] validSizes = { "S", "M", "L", "XL" };
		String trimmed = str.substring(str.lastIndexOf(" ")+1);
		//need to change lower case to upper case
		if (Arrays.asList(validSizes).contains(trimmed)) {
			String line = str.replace(trimmed, "") + '\t' + trimmed;
			return line;
		}
		return str;
	}
}
class SummaryCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		Map<String, TaskStream> taskInfoMap = ParsingLog.readLog();

		// Iterate over the entries in the map and print each TaskInfo
		for (Map.Entry<String, TaskStream> entry : taskInfoMap.entrySet()) {
			System.out.println("Task Name: " + entry.getKey());
			System.out.println("Task Info: " + entry.getValue());
			System.out.println();
		}
	}
	//parsing code?? 3 options
	//what if I add another summary option?
	//private PrintSummary
	/*
	@Override
	public String toString() {
		return  "TaskName='" + taskName + '\'' +
				"TaskDescription='" + taskDescription + '\'' +
				"TaskSize='" + taskSize + '\'' +
				"Total time on task ='" + taskTotalTime + '\'';
	}
	*/
}
class RenameCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		//Observer update
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ arg[2];
		Util.addLog(line);
	}
}
class SizeCommand implements CommandStrategy {
	@Override
	public void execute(String[] arg) {
		//Observer update
		final String line = arg[1] +'\t'+ arg[0] +'\t'+ arg[2];
		Util.addLog(line);
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
record TaskStream(
	String taskName, String command, String description, String size, 
									String startTime, String stopTime, String totalTime) {}

class ParsingLog {
	private static final String logfile = "TM.txt";

	//description has problem
	public static Map<String, TaskStream> readLog() {
		try {
			return Files.lines(Path.of(logfile))
					.map(ParsingLog::parseLogLine)
						.collect(Collectors.toMap(TaskStream::taskName, 
							taskInfo -> 
							taskInfo, (storedInfo, newInfo) -> new TaskStream(
								storedInfo.taskName(),
								newInfo.command(),
								storedInfo.description() + " " + newInfo.description(),
								newInfo.size().isEmpty() ? storedInfo.size() : newInfo.size(),
								!newInfo.startTime().isEmpty() ? newInfo.startTime() : storedInfo.startTime(),
								!newInfo.stopTime().isEmpty() ? newInfo.stopTime() : storedInfo.stopTime(),
								// make this task in diff class (Summary class)
								calculateTotalTime(storedInfo.startTime(), newInfo.stopTime(), storedInfo.totalTime())
							)
						));
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading log file", e);
		}
	}

	private static TaskStream parseLogLine(String line) {
		final String[] parts = line.split("\t");
		final String taskName = parts[1];
		final String command = parts[2];
		// need to change this
		final String description = parts.length > 3 ? parts[3] : "";
		final String size = parts.length > 4 ? parts[4] : "";

		String startTime = "";
		String stopTime = "";
		final String totalTime = "00:00:00";

		if ("start".equals(command)) {
			startTime = parts[0];
		} else if ("stop".equals(command)) {
			stopTime = parts[0];
		}

		return new TaskStream(taskName, command, description, size, startTime, stopTime, totalTime);
	}
	private static String calculateTotalTime(String startTime, String stopTime, String totalTime) {
		if (!startTime.isEmpty() && !stopTime.isEmpty()) {
			final LocalDateTime startDateTime = LocalDateTime.parse(startTime);
			final LocalDateTime stopDateTime = LocalDateTime.parse(stopTime);
			final Duration duration = Duration.between(startDateTime, stopDateTime);

			// add exist total time + new total time
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

	public static void addLog(String line) {
		final String DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
		final String timeStamp = new SimpleDateFormat(DATE_TIME_FORMAT_PATTERN).format(new Date());
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
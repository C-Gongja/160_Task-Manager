import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

//import de.siegmar.fastcsv.reader.NamedCsvReader;
//import de.siegmar.fastcsv.reader.NamedCsvRow;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

enum CurrentTaskState {}

record TaskInfo(
    String commentId, String commitId, String url, String author, String time, String body) {}

class Util {
	/*
	public static Stream<TaskInfo> readComments(Path csvPath) {
		try {
			return NamedCsvReader.builder().build(csvPath).stream()
				.map(
					(NamedCsvRow csvRow) -> {
						return new GitHubComment(
							csvRow.getField("COMMENT_ID"),
							csvRow.getField("COMMIT_ID"),
							csvRow.getField("URL"),
							csvRow.getField("AUTHOR"),
							csvRow.getField("CREATED_AT"),
							csvRow.getField("BODY"));
					});
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	 */
	public static String getProject(TaskInfo issue) {
		String[] items = issue.url().split("/");
		if (items.length < 3) {
			return null;
		}
		return (items[3] + "/" + items[4]);
	}

  	// Observer update
	public static boolean makeFile(String _fileName, boolean bAppend) {
		boolean isOK;
		try{
			FileWriter fw=new FileWriter(_fileName, bAppend);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(line);
			bw.newLine();
			bw.flush();
			bw.close();
			fw.close();
			isOK= true;
		}
		catch (Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		return isOK;	
	}
}

// State Pattern
interface TaskState {
	void start(String taskName);
	void stop(String taskName);
	void delete(String taskName);
}

class StartState implements TaskState {
	private static StartState instance;
	private static final String logfile = "TM.log";

    private StartState() { }

    public static StartState getInstance() {
        if(instance == null) {
			instance = new StartState();
		}	
		return instance;
	}
	
	@Override
	public void start(String taskName) {
		//Observer update
		final String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
		final String line = timeStamp +'\t'+ taskName +'\t'+ "start";
		Util.makeFile(logfile, line, true);
	}

	@Override
	public void stop(String taskName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'stop'");
	}

	@Override
	public void delete(String taskName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'delete'");
	}
}

class StopState implements TaskState {
	private static final String logfile = "TM.log";
	@Override
	public void start(String taskName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'start'");
	}

	@Override
	public void stop(String taskName) {
		//Observer update
		final String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
		final String line = timeStamp +'\t'+ taskName +'\t'+ "stop";
		Util.makeFile(logfile, line, true);
	}

	@Override
	public void delete(String taskName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'delete'");
	}
}

class Task {
	private TaskState currentState;

	public void setState(TaskState state) {this.currentState = state;}

	public void start(String taskName) {currentState.start(taskName);}

	public void stop(String taskName) {currentState.stop(taskName);}
}

// Strategy Pattern
interface CommandStrategy {
	void execute(String... args);
}

class DescribeCommand implements CommandStrategy {

	@Override
	public void execute(String... args) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'execute'");
	}
}

class SummaryCommand implements CommandStrategy {

	@Override
	public void execute(String... args) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'execute'");
	}
}

class RenameCommand implements CommandStrategy {

	@Override
	public void execute(String... args) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'execute'");
	}
}

class SizeCommand implements CommandStrategy {

	@Override
	public void execute(String... args) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'execute'");
	}
}

class TaskStrategy {
	private CommandStrategy currentStrategy;

	public void setStrategy(CommandStrategy strategy) {
		this.currentStrategy = strategy;
	}

	public void executeCommand(String... args) {
		currentStrategy.execute(args);
	}
}

// Main Program
public class TM {
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java TM.java <command> <data>");
			return;
		}

		final String cmd = args[0];
		final String data = args[1];

		Task task = new Task();
		TaskStrategy tm = new TaskStrategy();

		task.setState(new StartState());
		tm.setStrategy(new DescribeCommand());

		// Set initial state or strategy
		switch (cmd) {
			case "start":
				task.start(data);
			case "stop":
				task.stop(data);
			case "describe":
				tm.executeCommand("describe", "Task1", "Description", "L");
			case "summary":
				
			case "rename":
				
		}
	}
}

/*import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.stream.Stream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import java.io.IOException;
import java.io.UncheckedIOException;

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

// Strategy Pattern
interface CommandStrategy {
	void execute(String[] data);
}
class StartCommand implements CommandStrategy {
	@Override
	public void execute(String[] data) {
		//Observer update
		final String line = data[1] +'\t'+ data[0];
		Util.makeFile(line);
	}
}
class StopCommand implements CommandStrategy {
	@Override
	public void execute(String[] data) {
		//Observer update
		final String line = data[1] +'\t'+ data[0];
		Util.makeFile(line);
	}
}
class DeleteCommand implements CommandStrategy {
	@Override
	public void execute(String[] data) {
		//Observer update
		final String line = data[1] +'\t'+ data[0];
		Util.makeFile(line);
	}
}
class DescribeCommand implements CommandStrategy {
	@Override
	public void execute(String[] data) {
		//Observer update
		final String description = String.join(" ", Arrays.copyOfRange(data, 2, data.length));
		//check optional [size] command
		final String line = data[1] +'\t'+ data[0] +'\t'+ isSizeIncluded(description);
		Util.makeFile(line);
	}

	//what if I add another command to describe?
	private String isSizeIncluded(String str) {
		String[] validSizes = { "S", "M", "L", "XL" };
		String trimmed = str.substring(str.lastIndexOf(" ")+1);
		
		if (Arrays.asList(validSizes).contains(trimmed)) {
			String line = str.replace(trimmed, "") + '\t' + trimmed;
			return line;
		}
		return str;
	}
}
class SummaryCommand implements CommandStrategy {
	@Override
	public void execute(String[] data) {
		Map<String, TaskInfo> taskInfoMap = Util.readLog();

		// Iterate over the entries in the map and print each TaskInfo
		for (Map.Entry<String, TaskInfo> entry : taskInfoMap.entrySet()) {
			System.out.println("Task Name: " + entry.getKey());
			System.out.println("Task Info: " + entry.getValue());
			System.out.println();
		}
	}
	//parsing code?? 3 options
	//what if I add another summary option?
	//private PrintSummary
}
class RenameCommand implements CommandStrategy {
	@Override
	public void execute(String[] data) {
		//Observer update
		final String line = data[1] +'\t'+ data[0] +'\t'+ data[2];
		Util.makeFile(line);
	}
}
class SizeCommand implements CommandStrategy {
	@Override
	public void execute(String[] data) {
		//Observer update
		final String line = data[1] +'\t'+ data[0] +'\t'+ data[2];
		Util.makeFile(line);
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

class Task {
	private String taskName;
	private String taskStartTime;
	private String taskStopTime;
	private String taskDescription;
	private String taskSize;
	private String taskTotalTime;

	public Task(String taskName, String taskStartTime, String taskStopTime, String taskTotalTime, String taskDescription, String taskSize) {
		this.taskName = taskName;
		this.taskStartTime = taskStartTime;
		this.taskStopTime = taskStopTime;
		this.taskDescription = taskDescription;
		this.taskSize = taskSize;
		this.taskTotalTime = taskTotalTime;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getTaskStartTime() {
		return taskStartTime;
	}

	public String getTaskStopTime() {
		return taskStopTime;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public String getTaskSize() {
		return taskSize;
	}

	public String getTotalTime() {
		return taskTotalTime;
	}

	@Override
	public String toString() {
		return "Task{" +
				"taskName='" + taskName + '\'' +
				", taskStartTime='" + taskStartTime + '\'' +
				", taskStopTime='" + taskStopTime + '\'' +
				", taskDescription='" + taskDescription + '\'' +
				", taskSize='" + taskSize + '\'' +
				'}';
	}
}
// Using Stream
record TaskInfo(
	String taskName, String command, String description, String size, String startTime, String stopTime) {}

class Util {
	private static final String logfile = "TM.txt";
	
	public static Map<String, TaskInfo> readLog() {
		try {
			return Files.lines(Path.of(logfile))
					.map(Util::parseLogLine)
						.collect(Collectors.toMap(TaskInfo::taskName, 
								taskInfo -> taskInfo, 
									(storedInfo, newInfo) -> new TaskInfo(
										storedInfo.taskName(),
										newInfo.command(),
										storedInfo.description() + " " + newInfo.description(),
										newInfo.size(),
										storedInfo.startTime().isEmpty() ? newInfo.startTime() : storedInfo.startTime(),
										storedInfo.stopTime().isEmpty() ? newInfo.stopTime() : storedInfo.stopTime())
									));										
		} catch (IOException e) {
			throw new UncheckedIOException("Error reading log file", e);
		}
	}

	private static TaskInfo parseLogLine(String line) {
		String[] parts = line.split("\t");
		String taskName = parts[1];
		String command = parts[2];
		//may need for check describe command format
		//describe <task name> <description> [{S|M|L|XL}]
		String description = parts.length > 3 ? parts[3] : "";
		String size = parts.length > 4 ? parts[4] : "";
		String startTime = "";
		String stopTime = "";

		if ("start".equals(command)) {
			startTime = parts[0];
		} else if ("stop".equals(command)) {
			stopTime = parts[0];
		}

		return new TaskInfo(taskName, command, description, size, startTime, stopTime);
	}
	
	// Observer pattern? update
	public static void makeFile(String line) {
		final String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
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
} */
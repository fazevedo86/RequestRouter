package pt.ulisboa.tecnico.amorphous.requestrouter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SystemShell {

	public static List<String> executeCommand(String command) throws IOException, InterruptedException {
		ArrayList<String> output = new ArrayList<String>();
		String outputLine = "";
		
		Process cmd;
		cmd = Runtime.getRuntime().exec(command);
		cmd.waitFor();
		BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
		while ((outputLine = cmdOutput.readLine()) != null) {
			output.add(outputLine);
		}
		cmdOutput.close();

		return output;
	}
}

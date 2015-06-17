package pt.ulisboa.tecnico.amorphous.requestrouter.shell;

import java.io.IOException;

import pt.ulisboa.tecnico.amorphous.requestrouter.RequestRouter;
import asg.cliche.Command;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;

public class RequestRouterShell {

	public static final String PROMPT = "RequestRouter";
	
	private final RequestRouter context;
	private final ShellCommands shellCmds;
	private final Shell shell;
	
	public RequestRouterShell(RequestRouter context) {
		this.context = context;
		this.shellCmds = new ShellCommands(context);
		this.shell = ShellFactory.createConsoleShell(RequestRouterShell.PROMPT, "<> Amorphous RequestRouter <>", this.shellCmds);
	}
	
	public void startShell(){
		this.shell.setDisplayTime(true);
		try {
			this.shell.commandLoop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

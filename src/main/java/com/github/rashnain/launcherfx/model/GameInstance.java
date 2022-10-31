package main.java.com.github.rashnain.launcherfx.model;

import java.io.IOException;

public class GameInstance {
	
	private String command;
	
	private String gameDir;
	
	private Process process;
	
	public GameInstance(String gameDir) {
		this.command = "";
		this.gameDir = gameDir;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public void addCommand(String cmd) {
		addCommand(cmd, " ");
	}
	
	public void addCommand(String cmd, String lineEnd) {
		this.command += cmd + lineEnd;
	}
	
	public void runInstance() throws IOException {
		this.process = Runtime.getRuntime().exec(this.command);
	}
	
	public String getGameDir() {
		return this.gameDir;
	}
	
	public Process getProcess() {
		return this.process;
	}
}

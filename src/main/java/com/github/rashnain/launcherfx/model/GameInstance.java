package main.java.com.github.rashnain.launcherfx.model;

import java.io.IOException;
import java.util.Scanner;

/**
 * Class representing a game instance
 */
public class GameInstance {

	private StringBuilder command;

	private String gameDir;

	private Process process;

	/**
	 * Create an instance in this directory<br>
	 * Used to know if a directory is already being used or not<br>
	 * To inform user that launching multiple instances in the same directory can cause bug
	 * @param gameDir Directory the game will work with
	 */
	public GameInstance(String gameDir) {
		this.gameDir = gameDir;
		this.command = new StringBuilder();
	}

	/**
	 * Execute the instance's command
	 * @throws IOException If an error occure when launching
	 */
	public void runInstance() throws IOException {
		this.process = Runtime.getRuntime().exec(getCommand());

		Thread t = new Thread() {
			public void run() {
				Scanner s = new Scanner(process.getInputStream());
				while (s.hasNext()) {
					s.nextLine();
				}
				System.out.println("Instance terminated.");
			}
		};

		t.start();
	}

	/**
	 * @return the instance's command
	 */
	public String getCommand() {
		return this.command.toString();
	}

	/**
	 * Adds a command
	 * @param cmd the command
	 * @param lineEnd end of the command
	 */
	public void addCommand(String cmd, String lineEnd) {
		this.command.append(cmd + lineEnd);
	}

	/**
	 * Adds a command, ending with a space
	 * @param cmd the command
	 */
	public void addCommand(String cmd) {
		addCommand(cmd, " ");
	}

	/**
	 * @return the instance's game directory
	 */
	public String getGameDir() {
		return this.gameDir;
	}

	/**
	 * @return instance's process
	 */
	public Process getProcess() {
		return this.process;
	}

}

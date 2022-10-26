package main.java.com.github.rashnain.launcherfx.types;

import java.util.HashMap;
import java.util.Map;

public class GameProfile {

	private String name;

	private String directory;
	
	private String version_id;
	
	private String version_type;
	
	private int[] resolution = new int[2];
	
	private String executable;
	
	private String jvmArgument;
	
	public GameProfile(String name, String version_id) {
		this.name = name;
		this.version_id = version_id;
	}
	
	public String toString() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public void setId(String id) {
		this.version_id = id;
	}

	public String getId() {
		return this.version_id;
	}

	public String getExecutable() {
		return this.executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public String getVersion_type() {
		return this.version_type;
	}

	public void setVersion_type(String version_type) {
		this.version_type = version_type;
	}

	public int[] getResolution() {
		return this.resolution;
	}

	public void setResolutions(int width, int height) {
		this.resolution[0] = width;
		this.resolution[1] = height;
	}

	public String getDirectory() {
		return this.directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public Map<String, String> toHashMap() {
		Map<String, String> map = new HashMap<>();
		map.put("--version", this.version_id);
		map.put("--gameDir", "/"+this.version_id+"/");
		map.put("--assetsDir", "/assets/");
		map.put("--assetIndex", "1.18");
		map.put("--versionType", "release");
		map.put("--width", String.valueOf(this.resolution[0]));
		map.put("--height", String.valueOf(this.resolution[1]));
		return map;
	}

	public String getJvmArgument() {
		return this.jvmArgument;
	}

	public void setJvm_argument(String jvm_argument) {
		this.jvmArgument = jvm_argument;
	}

}


package main.java.com.github.rashnain.launcherfx.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtility {

	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static JsonObject load(String path) throws IOException {
		FileInputStream in = new FileInputStream(path);
		StringBuilder sb = new StringBuilder();
		
		byte[] buffer = new byte[4048];
		byte[] subBuffer;
		int sizeBuffer;

		while ((sizeBuffer=in.read(buffer)) > 0) {
			if (sizeBuffer != buffer.length) {
				subBuffer = new byte[sizeBuffer];
				for (int i = 0; i < sizeBuffer; i++) { 
					subBuffer[i] = buffer[i];
				}
				sb.append(new String(subBuffer));
			} else {
				sb.append(new String(buffer));
			}
			buffer = new byte[1024];
		}
		
		in.close();
		
		return JsonParser.parseString(sb.toString()).getAsJsonObject();
	}

	public static boolean save(String path, JsonElement json) {
		try {
			System.out.println("Saving settings into launcher_profiles.json.");
			
			File file = new File(path);
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			out.write(gson.toJson(json).getBytes());
			out.close();
			
			System.out.println("Saved settings.");
			return true;
		} catch (Exception e) {
			System.out.println("Couldn't save profile");
			e.printStackTrace();
			return false;
//			Runtime.getRuntime().exit(1);
		}
	}
}

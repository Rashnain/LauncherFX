package com.github.rashnain.launcherfx.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Utility class to load and save JSON files
 */
public class JsonUtility {

	/**
	 * Gson instance with pretty printing and disabled HTML escaping (for keeping as is "%")
	 */
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	/**
	 * Returns a JsonObject corresponding to the content of the specified file
	 * @param path Path to the json file
	 * @return JsonObject or null if the file is not found
	 */
	public static JsonObject load(String path) {
		try {
			FileInputStream in = new FileInputStream(path);
			StringBuilder sb = new StringBuilder();
	
			byte[] buffer = new byte[4048];
			byte[] subBuffer;
			int sizeBuffer;

			while ((sizeBuffer=in.read(buffer)) > 0) {
				if (sizeBuffer != buffer.length) {
					// Without this, "0" bytes are translated to " " and cause bugs
					subBuffer = new byte[sizeBuffer];
					for (int i = 0; i < sizeBuffer; i++) {
						subBuffer[i] = buffer[i];
					}
					sb.append(new String(subBuffer));
				} else {
					sb.append(new String(buffer));
				}
				buffer = new byte[4048];
			}
	
			in.close();
	
			return JsonParser.parseString(sb.toString()).getAsJsonObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Save a JsonElement to a file
	 * @param path the file where the JsonElement will be saved
	 * @param json the JsonElement to save
	 * @return false if an IOException occurred, true otherwise
	 */
	public static boolean save(String path, JsonElement json) {
		try {
			File file = new File(path);
			file.createNewFile();

			FileOutputStream out = new FileOutputStream(file);
			out.write(gson.toJson(json).getBytes());
			out.close();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}

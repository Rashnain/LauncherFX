package main.java.com.github.rashnain.launcherfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import main.java.com.github.rashnain.launcherfx.types.LauncherProfile;

public class Util {
	
	public static final String VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

	public static void changeRoot(String file, ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(LauncherFX.class.getResource("view/" + file + ".fxml"));
		Locale locale = new Locale(LauncherProfile.getProfile().getLocale());
		loader.setResources(ResourceBundle.getBundle("main.java.com.github.rashnain.launcherfx.locales.lang", locale));
		((Node) event.getSource()).getScene().setRoot(loader.load());
	}
	
	/**
	 * Download file at URL if dir/filename doesn't already exist
	 * @param url
	 * @param filename
	 * @param dir
	 * @throws IOException 
	 */
	public static void downloadFile(String url, String filename, String dir, int size) throws IOException {
		System.out.println("Downloading " + filename + " to " + dir);
		File pathFile = new File(dir);
		pathFile.mkdirs();
		File file = new File(dir+filename);
		if (file.createNewFile()) {// || file.getTotalSpace() != size) {
			URLConnection conn = new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "Wget/1.21.3 (linux-gnu)");

			InputStream in = conn.getInputStream();
			FileOutputStream out = new FileOutputStream(file);
			
			byte[] buffer = new byte[1024];
			byte[] subBuffer;
			int sizeBuffer;

			while ((sizeBuffer=in.read(buffer)) > 0) {
				if (sizeBuffer != buffer.length) {
					subBuffer = new byte[sizeBuffer];
					for (int i = 0; i < sizeBuffer; i++) { 
						subBuffer[i] = buffer[i];
					}
					out.write(subBuffer);
				} else {
					out.write(buffer);
				}
				buffer = new byte[1024];
			}
			
			out.close();
		}
	}

	public static JsonObject loadJSON(String path) throws IOException {
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
	
	public static boolean ruleSaysYes(JsonObject rule) {
		boolean useLib = false;
		
		if (rule.get("action").getAsString().equals("allow")) {
			useLib = true;
		} else if (rule.get("action").getAsString().equals("disallow")) {
			useLib = false;
		}
		
		String os = System.getProperty("os.name").toLowerCase();
		
		if (rule.keySet().contains("os")) {
			String name = rule.get("os").getAsJsonObject().get("name").getAsString();
			if (name.equals("windows") && os.contains("windows")) {
				return useLib;
			} else if (name.equals("osx") && os.contains("darwin")) {
				return useLib;
			} else if (name.equals("linux") && os.contains("linux")) {
				return useLib;
			}
		}
		
		return !useLib;
	}
	
	public static boolean shouldUseLibrary(JsonObject lib) {
		if (!lib.keySet().contains("rules")) {
			return true;
		}
		
		for (JsonElement e : lib.get("rules").getAsJsonArray()) {
			if (ruleSaysYes(e.getAsJsonObject())) {
				return true;
			}
		}
		
		return false;
	}

	public static String getNativesString(JsonObject lib) throws Exception {
		String nativesString = "";
		
		if (!lib.keySet().contains("natives")) {
			return nativesString;
		}
		
		/*String arch = System.getProperty("sun.arch.data.model");
		if (!arch.equals("64") && !arch.equals("32")) {
			throw new Exception("Architecture not supported.");
		}*/
		
		String os = System.getProperty("os.name").toLowerCase();
		
		JsonObject natives = lib.get("natives").getAsJsonObject();
		
		if (natives.keySet().contains("windows") && os.contains("windows")) {
			nativesString = natives.get("windows").getAsString();
		} else if (natives.keySet().contains("osx") && os.contains("darwin")) {
			nativesString = natives.get("osx").getAsString();
		} else if (natives.keySet().contains("linux") && os.contains("linux")) {
			nativesString = natives.get("linux").getAsString();
		} else {
			throw new Exception("Platform not supported.");
		}
		
		return nativesString;
	}
}

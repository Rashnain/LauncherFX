package main.java.com.github.rashnain.launcherfx.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LibraryUtility {

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

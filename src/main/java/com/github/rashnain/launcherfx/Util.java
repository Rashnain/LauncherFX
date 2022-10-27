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
import com.google.gson.JsonParser;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class Util {
	
	public static final String VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

	/**
     * Download and return the JsonElement specified by the url parameter
     * @param url Link to a JSON file
     * @return JsonElement
     * @throws IOException I/O exception
     */
	public static JsonElement downloadJSON(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "Wget/1.21.3 (linux-gnu)");
        InputStream stream = conn.getInputStream();

        byte[] buffer = new byte[1024];
        byte[] subBuffer;
        StringBuilder jsonSB = new StringBuilder();
        int size;//, subSize;

        
        while ((size=stream.read(buffer)) > 0) {
        	if (size != buffer.length) {
        		//System.out.println(buffer[size-1]);
        		//System.out.println(buffer[size]);
        		//if (buffer[size] == -1) {
        		//	subSize = size-1;
        		//} else {
        		//	subSize = size;
        		//}
        		subBuffer = new byte[size];
        		for (int i = 0; i < size; i++) { 
					subBuffer[i] = buffer[i];
				}
        		jsonSB.append(new String(subBuffer, "UTF-8"));
        	} else {
        		jsonSB.append(new String(buffer, "UTF-8"));
        	}
        	buffer = new byte[1024];
        }

        JsonElement jsonElement = JsonParser.parseString(jsonSB.toString());

        return jsonElement;
    }

	public static void changeRoot(String file, ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(LauncherFX.class.getResource("view/" + file + ".fxml"));
		// TODO need de use the user selected language
    	Locale locale = new Locale(Locale.getDefault().toLanguageTag());
        loader.setResources(ResourceBundle.getBundle("main.java.com.github.rashnain.launcherfx.locales.lang", locale));
    	((Node) event.getSource()).getScene().setRoot(loader.load());
	}
	
	/**
	 * Download file at URL if path/filename doesn't exists
	 * @param URL
	 * @param filename
	 * @param dir
	 * @throws IOException 
	 */
	public static void downloadFile(String URL, String filename, String dir, int size) throws IOException {
		System.out.println("Downloading " + filename + " to " + dir);
		File pathFile = new File(dir);
		pathFile.mkdirs();
		File file = new File(dir+filename);
		if (file.createNewFile()) {// || file.getTotalSpace() != size) {
			URLConnection conn = new URL(URL).openConnection();
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

	public static JsonElement loadJSON(String path) throws IOException {
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
        
		return JsonParser.parseString(sb.toString());
	}

}

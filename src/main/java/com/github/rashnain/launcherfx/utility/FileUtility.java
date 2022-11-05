package main.java.com.github.rashnain.launcherfx.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileUtility {

	/**
	 * Download file at URL if dir/filename doesn't already exist
	 * @param url
	 * @param filename
	 * @param dir
	 * @throws IOException 
	 */
	public static void download(String url, String filename, String dir, int size) throws IOException {
		File pathFile = new File(dir);
		pathFile.mkdirs();
		File file = new File(dir+filename);
		if (file.createNewFile() || size == 0) {
			System.out.println("Downloading " + filename + " to " + dir);
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
}

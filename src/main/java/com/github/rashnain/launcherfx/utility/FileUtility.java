package com.github.rashnain.launcherfx.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class to download files
 */
public class FileUtility {

	/**
	 * Download the file from url to a specified directory with a specified filename<br>
	 * If the file already exists and 'size' is zero or greater than the already existing file then it will be overwritten
	 * @param url Where the file is located on the Internet
	 * @param filename The name of the downloaded file
	 * @param dir The directory where the file will be downloaded to
	 * @param size The size of the file to download 
	 * @throws IOException If the file can't be downloaded
	 */
	public static void download(String url, String filename, String dir, long size) throws IOException {
		File path = new File(dir);
		path.mkdirs();
		File file = new File(dir+filename);
		if (file.createNewFile() || size == 0 || size > file.length()) {
			System.out.println("Downloading " + filename + " to " + dir);
			URLConnection conn = new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "Wget/1.21.3 (linux-gnu)");

			InputStream in = conn.getInputStream();
			FileOutputStream out = new FileOutputStream(file);

			byte[] buffer = new byte[4048];
			int len;

			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			out.close();
		}
	}

	/**
	 * Download the file from url to a specified directory with a specified filename<br>
	 * If the file already exists it will be overwritten
	 * @param url Where the file is located on the Internet
	 * @param filename The name of the downloaded file
	 * @param dir The directory where the file will be downloaded to
	 * @throws IOException If the file can't be downloaded
	 */
	public static void download(String url, String filename, String dir) throws IOException {
		download(url, filename, dir, 0);
	}

	/**
	 * Unzip files from a zip archive into a specified repertory, excluding specified files/folders
	 * @param zipFile path to the zip archive
	 * @param dest destination path
	 * @param exclusion list of files/folders to ignore
	 * @throws IOException if file manipulation error
	 */
	public static void unzip(String zipFile, String dest, List<String> exclusion) throws IOException {
		File destDir = new File(dest);
		destDir.mkdirs();
		byte[] buffer = new byte[4048];
		ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry zipEntry = zip.getNextEntry();
		while (zipEntry != null) {
			System.out.println(zipEntry.getName());
			boolean shouldExtract = true;
			for (String s : exclusion) {
				if (zipEntry.getName().contains(s)) {
					shouldExtract = false;
					break;
				}
			}
			if (shouldExtract) {
				File newFile = new File(destDir + "/" + zipEntry.getName());
				System.out.println(newFile.getAbsolutePath());
				if (zipEntry.isDirectory()) {
					newFile.mkdirs();
				} else {
					FileOutputStream out = new FileOutputStream(newFile);
					int len;
					while ((len = zip.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					out.close();
				}
			}
			zipEntry = zip.getNextEntry();
		}
		zip.closeEntry();
		zip.close();
	}

}

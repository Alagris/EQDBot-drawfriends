package alagris;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class Downloader {

	
	private final String path;

	public Downloader(final String downloadsDir) {
		path=downloadsDir;
	}
	boolean exportSource=false;
	public boolean isExportSource() {
		return exportSource;
	}
	public void setExportSource(final boolean exportSource) {
		this.exportSource = exportSource;
	}
	public void saveImage(final String url) throws IOException
	{
		String fileName=Utils.getFileNameFromURL(url);
		if(isExportSource()){
			fileName=Utils.getDomain(url)+" - "+fileName;
		}
		saveImageWithName(url, fileName);
	}
	private void saveImageWithName(final String fileURL,final String fileName) throws IOException
	{
		final File destinationFile =new File(path, fileName);
		Main.logln("Downloading from: " + fileURL);
		Main.logln("Creating file: " + destinationFile.getPath());
		if (destinationFile.exists())
		{
			Main.logln("This file already exists! Downloading stopped");
			return;
		}
		destinationFile.createNewFile();
		try (InputStream is = new URL(fileURL).openStream(); OutputStream os = new FileOutputStream(destinationFile))
		{
			
			final byte[] b = new byte[2048];
			int length;
			
			while ((length = is.read(b)) != -1)
			{
				os.write(b, 0, length);
			}
			
			is.close();
			os.close();
		}
		catch (final IOException e)
		{
			Main.logln("This file no longer exists in the internet! Downloading stopped");
		}
	}
}

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
	public String saveImage(final String prefix,final String url) throws IOException
	{
		return saveImage(prefix, url, new URL(url).openStream());
	}
	public String saveImage(final String prefix,final String url,final InputStream is) throws IOException
	{
		String fileName=Utils.getFileNameFromURL(url);
		if(isExportSource()){
			fileName=prefix+Utils.getDomain(url)+" - "+fileName;
		}else{
			fileName=prefix+fileName;
		}
		saveImageWithName(url, fileName,is);
		return fileName;
	}
	private void saveImageWithName(final String fileURL,final String fileName,final InputStream is) throws IOException
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
		try (OutputStream os = new FileOutputStream(destinationFile))
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

package alagris;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Utils {
	public static boolean checkDomain(final String sourceURL, final String domain){
		return getDomain(sourceURL).endsWith(domain);
	}
	public static String getDomain(final String sourceURL){
		return sourceURL.split("//", 2)[1].split("/", 2)[0];
	}
	public static String readFirstLineOfFile(final String path){
		
		try(Scanner sc = new Scanner(new File("startLink")))
		{
			return sc.nextLine();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getFileNameFromURL(final String url) {
		final String[] parts= url.split("/");
		return  parts[parts.length-1];
	}
	public static File getFileFromURL(final String parentDir,final String url) {
		return  new File(parentDir,getFileNameFromURL(url));
	}
	public static boolean isInteger(final String s){
		for(int i=0;i<s.length();i++){
			if(!Character.isDigit(s.charAt(i)))return false;
		}
		return true;
	}
}

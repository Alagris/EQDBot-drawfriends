package alagris;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public  class Bot 
{
	private final WebClient webClient;
	private final Downloader downloader;
	

	public Downloader getDownloader() {
		return downloader;
	}
	private static final String	downloadDir	= "images";
	public static String getDownloadDir() {
		return downloadDir;
	}

	public String getStartURL() {
		return startURL;
	}
	private final String	startURL;
	
	public Bot()
	{
		webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getCookieManager().setCookiesEnabled(true);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setJavaScriptEnabled(false);
		new File(downloadDir).mkdirs();
		startURL=Utils.readFirstLineOfFile("startLink");
		downloader = new Downloader(getDownloadDir());
	}
	
	
	
	public HtmlPage getPage(final String url)
	{
		try
		{
			return webClient.getPage(url);
		}
		catch (final FailingHttpStatusCodeException e)
		{
			e.printStackTrace();
		}
		catch (final MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		System.exit(-1);
		return null;
	}

	public HtmlPage getStartPage() {
		return getPage(getStartURL());
	}
	
	
}

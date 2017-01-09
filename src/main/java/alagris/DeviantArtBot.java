package alagris;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class DeviantArtBot {
	private final Bot bot;

	public DeviantArtBot( final Bot bot) {
		this.bot = bot;
	}

	public boolean download(final String prefix,final String url) throws IOException {
		Main.logln("\tDownloading from DeviantArt: " + url);
		if (!checkIfDeviantArtDomain(url)) {
			Main.logln("Not DeviantArt!");
			return false;
		}
		final String bigImageURL = extractPathToBiggestPossibleImage(url);
		
		Main.logln("Image src found: " + bigImageURL);
		if (bigImageURL == null) {
			Main.errln("ERROR4! DeviantArtBot");
			return false;
		}
		
//		final Page page = bot.getAnyPage(bigImageURL);
//		if(page==null||page.isHtmlPage()){
//			Main.errln("ERROR9! DeviantArtBot");
//			return false;
//		}
		bot.getDownloader().saveImage(prefix,bigImageURL);
		
		return true;
	}

	private boolean checkIfDeviantArtDomain(final String url) {
		return Utils.checkDomain(url, "deviantart.com");
	}

	private String extractPathToBiggestPossibleImage(final String url) {
		final HtmlPage page = bot.getPage(url);
		String path = extractPathFromDownloadButton(page);
		if (path == null){
			Main.logln("No download button found! Trying another way...");
			path = extractPathFromView(page);
		}
		return path;
	}

	private String extractPathFromDownloadButton(final HtmlPage page) {
		final String xPath = "//a[@class='dev-page-button dev-page-button-with-text dev-page-download']";
		final List<?> download = page.getByXPath(xPath);
		if (download.size() == 0) {
			Main.errln("ERROR3! DeviantArtBot");
			return null;
		}
		try {
			return ((HtmlAnchor) download.get(0)).click().getUrl().toString();
		} catch (final IOException e) {
			Main.errln("ERROR7! DeviantArtBot");
			return null;
			
		}
	}

	private String extractPathFromView(final HtmlPage page) {
		final String xPath = "//div[@class='dev-view-main-content']/div[@class='dev-view-deviation']/img[@class='dev-content-full ']";
		final List<?> img = page.getByXPath(xPath);
		if (img.size() == 0) {
			Main.errln("ERROR2! DeviantArtBot");
			return null;
		}
		return ((HtmlElement) img.get(0)).getAttribute("src");
	}
	
}

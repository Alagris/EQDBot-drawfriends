package alagris;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHorizontalRule;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class EQDBot {
	private final Bot bot;
	private volatile boolean isRunning=false;

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(final boolean isRunning) {
		this.isRunning = isRunning;
	}

	public EQDBot(final Bot bot) {
		this.bot = bot;
	}

	public void start(final boolean hiddenVersion, final boolean lowQuality, final String singlePostURL) {
		isRunning=true;
		if(singlePostURL==null){
			start(hiddenVersion, lowQuality);
		}else{
			try {
				getImagesInPost(singlePostURL, hiddenVersion, lowQuality);
				Main.logln("End of this post! My work is done! Bye!");
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void start(final boolean hiddenVersion, final boolean lowQuality) {
		Main.logln("Start url: "+bot.getStartURL());
		try {
			Main.logln("Connecting to EQD ");
			HtmlPage page = bot.getStartPage();
			while (isRunning) {
				Main.logln("Searching in group of posts: " + page.getUrl());
				final HtmlAnchor buttonForOlderPosts=searchAcrossEqdDrawfriendPosts(page, hiddenVersion,lowQuality);
				if(buttonForOlderPosts==null){
					Main.logln("That's it my friend! No more posts!");
					return;
				}
				page = buttonForOlderPosts.click();
			}

		} catch (final FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private HtmlAnchor searchAcrossEqdDrawfriendPosts(final HtmlPage page, final boolean hiddenVersion, final boolean lowQuality) throws IOException {
		final List<?> listOfBlogPosts = page.getByXPath("//h3[@class='post-title entry-title']/a");
		for (final Object blogPost : listOfBlogPosts) {
			final HtmlAnchor blogPostAnchor = (HtmlAnchor) blogPost;
			final String blogPostURL = blogPostAnchor.getAttribute("href");
			getImagesInPost(blogPostURL, hiddenVersion,lowQuality);
			if(!isRunning())break;
		}
		return page.getHtmlElementById("Blog1_blog-pager-older-link");
	}

	private void getImagesInPost(final String url, final boolean hiddenVersion, final boolean lowQuality) throws IOException {
		Main.logln("\nOpening post : " + url);
		final HtmlPage page = bot.getPage(url);
		final String prefix=getDrawfriendStuffPrefix(url);
		// visible images are under .../<div>/<a>/<img>
		// hidden images are under .../<div>/<a>
		final String imgXPath = "//div[@class='post-body entry-content']/div/a";
		final String srcXPath = "//div[@class='post-body entry-content']/b/a";
		final String breakXPath = "//div[@class='post-body entry-content']/hr";
		// sources are under .../<b>/<a>
		// layout is following:
		// img
		// src
		// break
		// src
		// img
		// img
		// img...
		// break
		// src
		// img
		// img
		// img...
		// break
		// src
		// img
		// img
		// img...
		// break
		// src
		// img
		// img
		// img...
		// break
		// ...
		// break
		// break (possible errors on page and breaks may be sometimes
		// unnecessary)

		final List<?> nodes = page.getByXPath(imgXPath + "|" + srcXPath + "|" + breakXPath);
		for (int i = 0, j = 0; i < nodes.size() && isRunning(); j++, i = j) {

			while (j < nodes.size()) {
				if (nodes.get(j) instanceof HtmlHorizontalRule) {
					break;
				}
				j++;
			}
			// node at index j the the node that should not interest us and we
			// should
			// not dereference it (as it could result in index out of bounds)

			// Nodes that we are interested in are between:
			// i inclusive
			// j exclusive
			// we also know that source <a> is always the last or the first in
			// the section
			// (sections) are divided by <hr>
			if (i != j)
				processNodesOfSection(nodes, i, j, hiddenVersion,lowQuality,prefix);

		}
	}

	/**
	 * You need to ensure that all nodes between i and j are or type HtmlAnchor
	 * 
	 * @param hiddenVersion
	 * @param lowQuality 
	 * @param postID 
	 */
	private void processNodesOfSection(final List<?> nodes, final int i, final int j, final boolean hiddenVersion, final boolean lowQuality, final String prefix) {
		int fromInclusive;
		int toExclusive;
		int sourceIndex;
		String sourceURL;

		if (isAnchorWithSource(nodes.get(i))) {
			fromInclusive = i + 1;
			toExclusive = j;
			sourceIndex = i;
		} else if (isAnchorWithSource(nodes.get(j - 1))) {
			fromInclusive = i;
			toExclusive = j - 1;
			sourceIndex = j - 1;
		} else {
			Main.errln("ERROR1! Cound not find Source Anchor among nodes bewteen i=" + i + " j=" + j);
			return;
		}
		sourceURL = ((HtmlAnchor) nodes.get(sourceIndex)).getAttribute("href");
		processImagesOfSection(nodes, fromInclusive, toExclusive, sourceURL, hiddenVersion,lowQuality,prefix);

	}

	private void processImagesOfSection(final List<?> nodes, final int fromInclusive, final int toExclusive, final String sourceURL,
			final boolean hiddenVersion, final boolean lowQuality, final String prefix) {
		final int countOfImages = toExclusive - fromInclusive;
		Main.logln("count of images=" + countOfImages);
		switch (countOfImages) {
		case 0:
			Main.errln("ERROR5! EQDBot");
			return;
		case 1:
			if(lowQuality){
				downloadFromEqdPostImages(nodes, fromInclusive, toExclusive, hiddenVersion,prefix);
			}else
			if ( visibilityFilter((HtmlAnchor) nodes.get(fromInclusive), hiddenVersion)) {
				final DeviantArtBot deviantArtBot = new DeviantArtBot(bot);
				try {
					if(!deviantArtBot.download(prefix,sourceURL)){
						Main.logln("Downloading from DeviantArt failed! Switching to EQD!");
						downloadFromEqdPostImages(nodes, fromInclusive, toExclusive, hiddenVersion,prefix);
					}
				} catch (final IOException e) {
					Main.errln("ERROR6! EQDBot");
					downloadFromEqdPostImages(nodes, fromInclusive, toExclusive, hiddenVersion,prefix);
				}
			}
			break;
		default:
			downloadFromEqdPostImages(nodes, fromInclusive, toExclusive, hiddenVersion,prefix);
			break;
		}

	}

	private void downloadFromEqdPostImages(final List<?> nodes, int fromInclusive, final int toExclusive, final boolean hiddenVersion, final String prefix)
			 {
		while (fromInclusive < toExclusive) {
			
			final HtmlAnchor aImage = (HtmlAnchor) nodes.get(fromInclusive);
			if (visibilityFilter(aImage, hiddenVersion)) {
				final String imgURL = aImage.getAttribute("href");
				
				try {
					Main.logln("\tDownloading from EQD: " + imgURL);
					bot.getDownloader().saveImage(prefix,imgURL);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			fromInclusive++;
		}
	}

	////////////////////////////////////////////
	// Down below are utility methods
	////////////////////////////////////////////
	private boolean isAnchorWithSource(final Object node) {
		if (node instanceof HtmlAnchor) {
			return ((HtmlAnchor) node).getParentNode().getNodeName().equals("b");
		}
		return false;
	}

	private boolean isAnchorWithImg(final HtmlAnchor node) {
			return  node.getParentNode().getNodeName().equals("div");
	}
//
	/** First try whether it is an anchor with image at all */
	private boolean isAnchorWithVisibleImg(final HtmlElement node) {
		if (node.getChildElementCount() != 1)
			return false;
		final DomNode child = node.getFirstChild();
		return child.getNodeName().equals("img");
	}
	

	/** returns true if anchor with image passes test */
	private boolean visibilityFilter(final HtmlAnchor aImage, final boolean hiddenVersion) {
		// hidden version = we are searching for <a> element that have no
		// <img> inside them so the image is hidden
		// The if statement below is true:
		// if:
		// there is no image inside <a> and it's hidden version
		// or if:
		// there is one child (img is the only possibility unless they change
		// page layout)
		// and it's visible version.
		return isAnchorWithImg(aImage) && !(!isAnchorWithVisibleImg(aImage) ^ hiddenVersion);
	}
	
	/**Conventional prefix based on post ID*/
	private String getDrawfriendStuffPrefix(final String postURL){
		return getDrawfriendStuffID(postURL)+" ";
	}
	private String getDrawfriendStuffID(final String postURL){
		//the URL appears to be of this format:
		//http://www.equestriadaily.com/2013/12/drawfriend-stuff-1032.html
		//http://www.equestriadaily.com/2016/12/drawfriend-stuff-2117-art-compilation.html#more
		final String[] urlParts =postURL.split("/");
		final String[] postParts= urlParts[urlParts.length-1].split("-");
		if(postParts.length>=3){
			final String id = postParts[2];
			if(Utils.isInteger(id)){
				return id;
			}else{
				return "unknown_id";
			}
		}
		return "unknown_id";
	}

	

}

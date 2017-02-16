package alagris;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBold;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHorizontalRule;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class EQDBot {
	private final Bot bot;
	private volatile boolean isRunning = false;

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
		isRunning = true;
		if (singlePostURL == null) {
			start(hiddenVersion, lowQuality);
		} else {
			try {
				getImagesInPost(singlePostURL, hiddenVersion, lowQuality);
				Main.logln("End of this post! My work is done! Bye!");
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void start(final boolean hiddenVersion, final boolean lowQuality) {
		Main.logln("Start url: " + bot.getStartURL());
		HtmlPage page = null;
		try {
			Main.logln("Connecting to EQD ");
			page = bot.getStartPage();
			while (isRunning) {
				Main.logln("Searching in group of posts: " + page.getUrl());
				final HtmlAnchor buttonForOlderPosts = searchAcrossEqdDrawfriendPosts(page, hiddenVersion, lowQuality);
				if (buttonForOlderPosts == null) {
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
		Main.logln("Downloading finished!");
		if (page != null)
			Main.logln("To resume downloading from this point put the following link in 'startLink' file:\n"
					+ page.getUrl() + "\nThe default link is http://www.equestriadaily.com/search/label/Drawfriend");
	}

	private HtmlAnchor searchAcrossEqdDrawfriendPosts(final HtmlPage page, final boolean hiddenVersion,
			final boolean lowQuality) throws IOException {
		final List<?> listOfBlogPosts = page.getByXPath("//h3[@class='post-title entry-title']/a");
		for (final Object blogPost : listOfBlogPosts) {
			final HtmlAnchor blogPostAnchor = (HtmlAnchor) blogPost;
			final String blogPostURL = blogPostAnchor.getAttribute("href");
			getImagesInPost(blogPostURL, hiddenVersion, lowQuality);
			if (!isRunning())
				break;
		}
		return page.getHtmlElementById("Blog1_blog-pager-older-link");
	}

	private void getImagesInPost(final String url, final boolean hiddenVersion, final boolean lowQuality)
			throws IOException {

		final String id = getDrawfriendStuffID(url);
		Main.logln("\nOne more post. ID=" + id);
		final boolean isSaucyPost = id.equals(SAUCY_ID);
		if (!postFilter(isSaucyPost, hiddenVersion)) {
			Main.logln("Skipping post: " + url);
			return;
		}
		Main.logln("Opening post : " + url);
		final HtmlPage page = bot.getPage(url);
		final String prefix = getDrawfriendStuffPrefix(id);
		// visible images are under .../<div>/<a>/<img>
		// hidden images are under .../<div>/<a>
		final String imgXPath = "//div[@class='post-body entry-content']/div/a";
		// sources are under .../<b>/<a>
		// or in older posts .../<a>/<b>
		final String srcXPath = "//div[@class='post-body entry-content']/b/a";
		final String srcXPathOld = "//div[@class='post-body entry-content']/a/b";
		// breaks are under .../<hr>
		final String breakXPath = "//div[@class='post-body entry-content']/hr";

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

		final List<?> nodes = page.getByXPath(imgXPath + "|" + srcXPath + "|" + breakXPath + "|" + srcXPathOld);
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
				processNodesOfSection(nodes, i, j, hiddenVersion, lowQuality, prefix, isSaucyPost);

		}
	}

	/**
	 * You need to ensure that all nodes between i and j are or type HtmlAnchor
	 * 
	 * @param hiddenVersion
	 * @param lowQuality
	 * @param isSaucyPost
	 * @param postID
	 */
	private void processNodesOfSection(final List<?> nodes, final int i, final int j, final boolean hiddenVersion,
			final boolean lowQuality, final String prefix, final boolean isSaucyPost) {
		int fromInclusive;
		int toExclusive;
		String sourceURL = null;
		HtmlAnchor sourceAnchor = null;
		if ((sourceAnchor = isAnchorWithSource(nodes.get(i))) != null) {
			fromInclusive = i + 1;
			toExclusive = j;
		} else if ((sourceAnchor = isAnchorWithSource(nodes.get(j - 1))) != null) {
			fromInclusive = i;
			toExclusive = j - 1;
		} else {
			fromInclusive = i;
			toExclusive = j;
			// this is a safety check against missing links which
			// would be source anchors but they are missing <a> tag
			if (!isAnchorWithImg(nodes.get(fromInclusive))) {
				fromInclusive++;
			}
			if (!isAnchorWithImg(nodes.get(toExclusive - 1))) {
				toExclusive--;
			}
			Main.errln("ERROR1! Cound not find Source Anchor among nodes bewteen i=" + i + " j=" + j);
		}
		if (sourceAnchor != null)
			sourceURL = sourceAnchor.getAttribute("href");
		try {
			processImagesOfSection(nodes, fromInclusive, toExclusive, sourceURL, hiddenVersion, lowQuality, prefix,
					isSaucyPost);
		} catch (final Exception e) {// lets swallow exceptions because Internet
								// websites are hell
			e.printStackTrace();
		}

	}

	private void processImagesOfSection(final List<?> nodes, final int fromInclusive, final int toExclusive,
			String sourceURL, final boolean hiddenVersion, final boolean lowQuality, final String prefix,
			final boolean isSaucyPost) {
		final int countOfImages = toExclusive - fromInclusive;
		Main.logln("count of images=" + countOfImages);
		switch (countOfImages) {
		case 0:
			Main.errln("ERROR5! EQDBot");
			return;
		case 1:// If the source is DeviantArt
				// there can be only one image.
				// If source is another website then
				// it could contain multiple images but
				// we know that deviantArt shows 1 image at the time
			if (!(nodes.get(fromInclusive) instanceof HtmlAnchor))
				break;// safety check in
			// case of unexpected page layout
			final HtmlAnchor imgAnchor = (HtmlAnchor) nodes.get(fromInclusive);

			if (sourceURL == null) {// watch out! missing link!
				// maybe the link is at least preserved by image itself
				sourceURL = imgAnchor.getAttribute("href");
			}
			if (lowQuality) {
				downloadFromEqdPostImages(nodes, fromInclusive, toExclusive, hiddenVersion, prefix, isSaucyPost);
			} else if (visibilityFilterWithLog(imgAnchor, hiddenVersion, isSaucyPost)) {
				final DeviantArtBot deviantArtBot = new DeviantArtBot(bot);
				try {
					if (!deviantArtBot.download(prefix, sourceURL)) {
						Main.logln("Downloading from DeviantArt failed! Switching to EQD!");
						downloadFromEqdPostImages(nodes, fromInclusive, toExclusive, hiddenVersion, prefix,
								isSaucyPost);
					}
				} catch (final IOException e) {
					Main.errln("ERROR6! EQDBot");
					downloadFromEqdPostImages(nodes, fromInclusive, toExclusive, hiddenVersion, prefix, isSaucyPost);
				}
			}
			break;
		default:
			downloadFromEqdPostImages(nodes, fromInclusive, toExclusive, hiddenVersion, prefix, isSaucyPost);
			break;
		}

	}

	private void downloadFromEqdPostImages(final List<?> nodes, int fromInclusive, final int toExclusive,
			final boolean hiddenVersion, final String prefix, final boolean isSaucyPost) {

		while (fromInclusive < toExclusive) {
			try {
				final HtmlAnchor aImage = (HtmlAnchor) nodes.get(fromInclusive);
				if (visibilityFilterWithLog(aImage, hiddenVersion, isSaucyPost)) {
					final String imgURL = aImage.getAttribute("href");

					try {
						Main.logln("\tDownloading from EQD: " + imgURL);
						bot.getDownloader().saveImage(prefix, imgURL);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			} catch (final Exception e) {// lets swallow exceptions because
											// Internet websites are hell
				e.printStackTrace();
			}
			fromInclusive++;
		}
	}

	////////////////////////////////////////////
	// Down below are utility methods
	////////////////////////////////////////////
	/** Returns the anchor if it is anchor with source or null if it's not */
	private HtmlAnchor isAnchorWithSource(final Object node) {
		if (node instanceof HtmlAnchor) {// newer layout
			final HtmlAnchor a = (HtmlAnchor) node;
			final String parent = a.getParentNode().getNodeName();
			if (parent.equals("b")) {
				return a;
			}
		} else if (node instanceof HtmlBold) {// older layout
			final HtmlBold b = (HtmlBold) node;
			final String parent = b.getParentNode().getNodeName();
			if (parent.equals("a")) {
				return (HtmlAnchor) b.getParentNode();
			}
		}
		return null;
	}

	private boolean isAnchorWithImg(final Object node) {
		if (node instanceof HtmlAnchor)
			return isAnchorWithImg((HtmlAnchor) node);
		return false;
	}

	private boolean isAnchorWithImg(final HtmlAnchor node) {
		return node.getParentNode().getNodeName().equals("div");
	}

	//
	/** First try whether it is an anchor with image at all */
	private boolean isAnchorWithVisibleImg(final HtmlElement node) {
		if (node.getChildElementCount() != 1)
			return false;
		final DomNode child = node.getFirstChild();
		return child.getNodeName().equals("img");
	}

	private boolean visibilityFilterWithLog(final HtmlAnchor aImage, final boolean hiddenVersion,
			final boolean isSaucyPost) {
		if (visibilityFilter(aImage, hiddenVersion, isSaucyPost)) {
			return true;
		} else {
			Main.logln("Visibility filter rejected!");
			return false;
		}
	}

	/**
	 * returns true if anchor with image passes test
	 * 
	 * @param isSaucyPost
	 */
	private boolean visibilityFilter(final HtmlAnchor aImage, final boolean hiddenVersion, final boolean isSaucyPost) {
		// hidden version = we are searching for <a> element that have no
		// <img> inside them so the image is hidden
		// The if statement below is true:
		// if:
		// there is no image inside <a> and it's hidden version
		// or if:
		// there is one child (img is the only possibility unless they change
		// page layout)
		// and it's visible version.
		if (!isAnchorWithImg(aImage)) {// safety check
			return false;
		}
		// all images in saucy posts are treated as hidden.
		final boolean isHiddenImage = isSaucyPost || !isAnchorWithVisibleImg(aImage);
		return !(isHiddenImage ^ hiddenVersion);
	}

	private static final String SAUCY_ID = "saucy";

	private boolean postFilter(final boolean isSaucyPost, final boolean hiddenVersion) {
		if (isSaucyPost)
			return hiddenVersion;
		return true;
	}

	/** Conventional prefix based on post ID */
	private String getDrawfriendStuffPrefix(final String id) {
		return id + " ";
	}

	private String getDrawfriendStuffID(final String postURL) {
		// the URL appears to be of this format:
		// http://www.equestriadaily.com/2013/12/drawfriend-stuff-1032.html
		// http://www.equestriadaily.com/2016/12/drawfriend-stuff-2117-art-compilation.html#more
		/** parts that every URL has */
		final String[] urlParts = postURL.split("/");
		// let's cut off the extension like .html
		final String urlLastPart = urlParts[urlParts.length - 1];
		final String urlLastPartTruncated = urlLastPart.substring(0, urlLastPart.lastIndexOf('.'));
		/** Words that make up the post title */
		final String[] postParts = urlLastPartTruncated.split("-");

		// a safety check
		for (int i = 0; i < postParts.length; i++) {
			postParts[i] = postParts[i].toLowerCase();
		}
		String id = "unknown_id";
		// now onto the work
		for (int i = 0; i < postParts.length; i++) {
			if (i + 1 < postParts.length && postParts[i].equals("drawfriend")) {
				if (i + 2 < postParts.length && postParts[i + 1].equals("stuff")) {
					// okay so we've got a sequence of 'drawfriend stuff'
					// now it must be the ID or there is no ID at all
					if (Utils.isInteger(postParts[i + 2])) {
						id = postParts[i + 2];
						break;
					}

				}
			} else if (postParts[i].equals("saucy")) {
				id = SAUCY_ID;
			}
		}
		return id;

	}

}

package alagris;

import alagris.gui.MainGUI;
import javafx.application.Application;

public class Main {
	private static final Bot bot = new Bot();
	private static final EQDBot eqdBot = new EQDBot(bot);
	private static boolean consoleMode = false;//should be false
	public static final String VERSION = "1.3";

	public static void main(final String[] args) {
		System.out.println("EQDBot v" + VERSION);
		System.out.println("Pass --headless as parameter to run in console mode.");
		System.out.println("Pass --hidden-only as parameter to download  only visible images.");
		System.out.println("Pass -lq as parameter to download faster but with lower quality whenever possible.");
		System.out.println("Pass --single-post <URL to post> as parameter to download from only one post.");
		//here are default options
		boolean hiddenVersion = true;//should be false
		boolean lowQuality = false;//should be false
		 String singlePostURL = null;//should be null
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "--hidden-only":
				hiddenVersion = true;
				break;
			case "-lq":
				lowQuality = true;
				break;
			case "--headless":
				consoleMode = true;
				break;
			case "--single-post":
				i++;
				if(i<args.length){
					singlePostURL=args[i];
				}else{
					System.err.println("No post URL specified after --single-post !");
				}
				break;
			}
		}

		if (consoleMode) {
			System.out.println("You run in console mode! Hit Ctrl-C to stop whenever"
					+ " you want (note the last image may be corrupted as"
					+ " it didn't have enough time to be fully downloaded)!");
			eqdBot.start(hiddenVersion, lowQuality,singlePostURL);
		} else {

			// final boolean hFinalCopy = hiddenVersion;
			// final boolean qFinalCopy = lowQuality;
			// final Runnable botRunnable = () -> eqdBot.start(hFinalCopy,
			// qFinalCopy);
			// final Thread botThread = new Thread(botRunnable);

			final Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					Application.launch(MainGUI.class, args);

				}
			});
			t.start();
			while (!MainGUI.isReady()) {
				try {
					Thread.sleep(10);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
			// botThread.start();
			eqdBot.start(hiddenVersion, lowQuality,singlePostURL);
		}

	}

	private static boolean loggingAllowed = true;

	public static void logln(final String text) {
		if (loggingAllowed) {
			System.out.println(text);
			if (!consoleMode) {
				MainGUI.controller.logln(text);
			}
		}

	}

	public static void errln(final String text) {
		if (loggingAllowed) {
			System.err.println(text);
			if (!consoleMode) {
				MainGUI.controller.logln(text);
			}
		}
	}

	public static void stop() {
		eqdBot.setRunning(false);
	}

	public static void disableLogging() {
		loggingAllowed = false;
	}

	public static void forceStop() {
		System.exit(0);
	}

}

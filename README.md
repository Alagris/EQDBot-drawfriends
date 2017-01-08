This is a simple web crawler that scans EQD posts and extracts from them drawfriend stuff
pictures. There is a simple GUI but in fact it's not necessary to run the program. The
only prerequisite it Java 8 (probably not working on older versions but you can try).
There are a few modes that this application can be run in:

1. headless mode:
	By default it runs in GUI mode. In order to run it headless you need to run it from 
	terminal:
	java - jar ./EQDbot.jar --headless
	In order to stop application running in headless mode you need to hit Ctrl-C.
	Note the last image may be corrupted as it didn't have enough time to be fully downloaded
	when exiting this way.
2. hidden mode:
	By default it skips saucy,gore,spoilers and other hidden images. You can also run in
	hidden mode to skip all the visible images and download only hidden ones. You need to
	pass this command from terminal:
	java - jar ./EQDbot.jar --hidden-only      
3. low quality mode:
	By default the bot is optimized for DeviantArt. When it detects that the source of image
	comes from there, it will not download it from EQD but directly from original location.
	On DeviantArt images tend to have much bigger size (like 3000x2000 px) while on EQD are 
	only miniatures (like 700x500 px). However, better quality means more data to transfer
	so this mode is slower. if you don't care that much about extra pixels you download only
	from EQD (often but not always in worse quality). To do it pass to terminal:
	java - jar ./EQDbot.jar -lq 
	
IMPORTANT: prior to running any of the commands make sure that you are in the directory that
contains EQDBot.jar (the directory that you unzipped everything to). Useful command for doing
it is 'cd /path/to/dir'.
 
All these modes can be mixed. Just pass these parameters together. For example:
java - jar ./EQDbot.jar --headless --hidden-only

If you want to download absolutely everything you will need to run this bot twice.
Once in hidden mode (with --hidden-only) and once in visible mode (without --hidden-only).

By default the bot starts it search at:
http://www.equestriadaily.com/search/label/Drawfriend
But you can change it to something more specific like:
http://www.equestriadaily.com/search/label/Drawfriend?updated-max=2016-12-21T17:00:00-07:00&max-results=20&start=20&by-date=false
(just click "Older Posts" button at the bottom or choose date directly).
Such a new link you will need to put inside 'startLink' file and the run the bot.
Once the web crawler starts it will traverse only to older posts, until you stop it
or it encounters the very last page. Changes made to 'startLink' file won't affect the bot
once its work is started. 

The downloaded images are put inside 'images' directory. This folder is automatically generated on startup if it's not already there.

Download it here:
https://github.com/Alagris/EQDBot-drawfriends/raw/master/EQDBot.zip
(it is among files above)

Have fun. The source code is open. Send patches if you want. And also, sorry for so minimalist GUI. If you want access to hidden EQD content you will need to make friends with
terminal. 

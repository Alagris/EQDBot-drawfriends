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
4. single post mode:
	By default bot starts at link provided in 'startLink' file and proceed to older posts. 
	however you may want todownload images from only one single post. In this case this:
	java - jar ./EQDbot.jar --single-post <URL to post>
	will allow you to do exactly that! Replace <URL to post> with URL like this one:
	http://www.equestriadaily.com/2017/01/drawfriend-stuff-2132-art-gallery.html 
	
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
Such a new link you will need to put inside 'startLink' file and then run the bot.
Once the web crawler starts it will traverse only to older posts, until you stop it
or it encounters the very last page. Changes made to 'startLink' file won't affect the bot
once its work is started. 

The downloaded images are put inside 'images' directory. This folder is automatically generated on startup if it's not already there.

There is a file naming convention that helps you find the origin of image. Each file starts with mysterious number that is the id of drawfriend stuff post. For example here:
2132 vapor__profile_____speedpaint__by_the_butcher_x-daugrj0.png
The number 2132 in this case corresponds to post:
Drawfriend Stuff #2132 (Art Compilation)
However there are posts that don't have ID (like 'best' and a few 'art compilations'). In such cases special value
'unknown_id' is prepended instead of a number. For saucy editions an ID 'saucy' is prepended so that you are warned before
looking inside. 

Sometimes, when you close the GUI you may experience a lag. In fact it's not an error in the code but just waiting for the last image to complete downloading. The required time to finally shut down may depend on yout internet connection and size of image.


Download it here:
https://github.com/Alagris/EQDBot-drawfriends/raw/master/EQDBot.zip
(it is among files above)

Have fun. The source code is open. Send patches if you want. And also, sorry for so minimalist GUI. If you want access to hidden EQD content you will need to make friends with terminal. If you find bugs feel free to notify me by adding an issue on github. However, please note that EQD itself sometimes has errors (like missing or dead links, missing images etc.) or in some posts the layout is sightly changed. My bot is designed to detect most of them but it's still possible to get trapped. In other words, before reporting a bug, try to make sure that it's not a desired befaviour in a misleading environemt. Some errors only a human being can detect (or really advanced AI, which I don't have time to code and in fact is not necessary for such a small project).

The program currently has no icon and what you see is the standard Java logo. I am not artistically talented but if anyone of you is interested in painting one feel free to send me a link in Issue "Program icon proposals" ( https://github.com/Alagris/EQDBot-drawfriends/issues/1 )

PS. to guys from EQD: don't modify your page layout and HMTL tags too quickly :)




CHANGELOG:
- version 1.0
	- initial version
	- headless, low quality and hidden modes
	- basic GUI
- version 1.1
	- post ID prepended to file name
	- GUI window got a title with version
	- single post mode added
- version 1.2
	- now bot attempts to detect "Saucy Edition" posts
	- some bugs fixed
- version 1.3
	- added support for older EQD layout (posts from around 2015 are different)
- version 1.3.1
	- in previous version there was one value modified for debugging which made it impossible to run in visible mode. This debugging leftover was fixed.
	- a message with default startLink value will show at the termination of program.
- version 1.4
	- added extra mechanism (swallowing exceptions) for dealing with unexpected HTML "bugs". Now the bot won't crash but just pretend nothing happened and try to collect as much info as possible instead.
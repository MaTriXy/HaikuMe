# Overview
Say "haiku me" when wearing Google Glass and be shown a random haiku from the <a href="http://www.reddit.com/r/haiku">/r/haiku</a> subreddit.

![HaikuMe in the main Glass app menu](http://raw.github.com/romanows/HaikuMe/master/screenshots/haikuVoiceTriggerInMainMenu_small.png)
![Downloading a haiku from /r/haiku](http://raw.github.com/romanows/HaikuMe/master/screenshots/haikuDownloading_small.png)
![Example haiku display](http://raw.github.com/romanows/HaikuMe/master/screenshots/haikuExample_small.png)

Feedback and bugfixes are welcomed.

Brian Romanowski
romanows@gmail.com

# Content Warning
This app displays unfiltered internet content.
Any and all ~17 syllable combinations may be projected into your eye.
Please consume responsibly.

# Installation
Installing this app is just like installing any other Google Glass app at this point.
Detailed instructions can be found elsewhere on the internet, but an outline is given below.

First, you'll need to turn on debug mode on your Glass by going to "Settings / Device Info / Turn on Debug".
You'll need to install the Android SDK: http://developer.android.com/sdk/index.html.
You can install the APK file "HaikuMe.apk" by downloading it from the root of this Github repo and then running:

path/to/sdk/platform-tools/adb install HaikuMe.apk

You can uninstall by running the command:

path/to/sdk/platform-tools/adb uninstall com.pwnetics.glass.haiku

## Permissions
This app downloads content from Reddit and therefore needs internet access permissions:
* Access Network State -- determine whether the network is accessible
* Internet -- download haiku content
* Development -- required for custom voice command

# Licensing
This code is licensed under the Affero General Public License (AGPL) 3.0.
Please see the LICENSE file for full details.

# Misc
## Voice Trigger
"Haiku Me" is the default voice trigger but this can be changed by editing the string "start_haiku" in res/values/strings.xml.
"Haiku Me" isn't really compatible with Google's voice trigger guidelines, but a phrase like "show a haiku" conflicted with other apps during testing.

For me, aspirating the "h" in "Haiku" and pronouncing the phrase as all one word works best.

## Copyright
The copyrights on the displayed haikus are retained by their authors according to the Reddit terms of service.
The haikus are retrieved from Reddit when requested by the user of this app, so everyone's rights are respected.

## Code Story
Permalinks for /r/haiku submissions are scraped via the Reddit API using a separate tool.
Haiku submission titles that don't parse or have too many characters are filtered out.
The remaining permalinks are stored in strings.xml.

When the app is first installed and run, an SQLite database is populated with the permalinks via the HaikuDbAdapter.DatabaseHelper class.
Thereafter, whenever a user enters the HaikuActivity, a random permalink that has not already been visited is selected from the database.
This permalink is retrieved using the Reddit API, parsed, displayed to the user, and then marked as having been visited.

# Future Work
* Haikus should go on cards in the Glass timeline, but the current GDK does not support programmatic addition of static cards with arbitrary layouts.
* Error messages should be more poetic.
* Maybe it should pull short-form poetry from twitter, too?

## What does this do?
This application tries to port offsets of cheat codes or similar from source memory dumps to destination memory dumps to help cheat code creation/game modding.

## Where do I download the application?
[Here](Universal%20Offset%20Porter.jar?raw=true).

## How do I start this application?
[You need to have Java installed](https://www.youtube.com/watch?v=t58ZrfkI2PM).

## How do I use this?
You start the `JAR` file, select the source memory dump, destination memory dump and enter the offset to port. The other settings are optional for fine tuning. It is way too inefficient to process the entire memory dump down to the last cent so drawbacks have to be made.

## Why does it have to detect whether an offset is assembly or not?
A different porting strategy is chosen depending on the offset being data or executable code.

## Why does porting take so long?
If this happens either you're trying to port something which can't generally be ported, no good search template is found or you need to adjust the optional settings to better reflect the circumstances. Mostly it finishes below one second.

## Why is the ported address wrong?
This is possible since the application can't guarantee correctness so take results with a grain of salt. In the future this might improve but it is technically impossible to avoid those errors.

## Where do I report bugs?
[Here](issues).
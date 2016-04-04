# RbxFontMapGenCmdV2

This work provides the ability to upload and use custom fonts in ROBLOX.
It has been modified from M0RGOTH's work.

Features:

* Conflictless (no overlaps) fontmap generation.
* Fontmaps may use multiple images if a single image may be too big.
* Automatic uploading of images.
* Automatic creation of JSON and Lua JSON loaders for fonts.
* Loading of fonts into ROBLOX through the FontLibrary.
* Applying fonts to existing TextLabels through the ExtendedTextLabel.
* Scaling of fonts for sizes that aren't uploaded.

The following things have been changes have been made from M0RGOTH's work:

* + Uploading was added.
* + The command-line interface was added.
* + Fontmap and JSON generation was rewritten to prevent conflicts/overlaps.
* + Loading of fontmaps into ROBLOX was rewritten to support the new format.
* | The method of displaying fonts through ExtendedTextLabel was modified to support the new format.
* | The names and descriptions of methods in the FontLibrary were modified to fit the new format.
* - Old methods that no longer applied were removed from the FontLibrary.
* - The graphical interface was removed.

**This module is not compatible with the original FontLibrary and associated tools. It uses different generation and layout methods.**

	Args:
	--upload
	-u
	        Whether or not to upload the images to roblox, 1 or true
	        -u false
	--roblosecurity
	-r
	        Your .ROBLOSECURITY cookie that's used to upload images to your account
	        This is required if you set -u or --upload
	        -r "COOKIEGOESHERE"
	--local
	-l
	        Whether or not to save to a file
	        -l true
	--font
	-f
	        The name of the font to generate for
	        -f Arial
	        -f "Times New Roman"
	--size
	-s
	        Takes up to three arguments: minSize, maxSize, and iterationLength
	        -s 16
	        -s 16 20
	        -s 10 40 10
	--image
	-i
	        The name of the image file. FONT, SIZE, LOC, NUM have replacements
	                FONT: The name of the font (only alphanumerics)
	                SIZE: The fontsize integer
	                LOC: The location in the list of fontsizes to generate
	                NUM: The number this image is in the list of images for this font/size combo
	        -i "FONT-LOC-SIZE-NUM.png"
	        -i "FONT-image-SIZE-NUM.png"
	--json
	-j
	        The name of the json file. Same replacements as image
	        -j FONT-LOC-SIZE-NUM.json
	        -j FONT-image-SIZE.json
	--wait
	-w
	        The amount of time to wait between each generation/upload
	        -w 0
	        -w 5
	--luafile
	-y
	        The file that the lua output should be saved to
	        -y "FONT-LOC-SIZE.lua"
	--multiple
	-m
	        Whether or not to generate a Lua file for each font
	        -c 1
	        -c true
	--combined
	-c
	        Whether or not to generate a final lua file containing all font data
	        -c 0n   -c false
	--fiddler
	        Configure Java to use a proxy set to 127.0.0.1:8888
	        --fiddler

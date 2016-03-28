# RbxFontMapGenCmd
A ROBLOX custom fontmap generator and uploader.
A modified version of M0RGOTH's FontMapGen program and a modified version of M0RGOTH's FontLibrary.

M0RGOTH's original work provided:

* The ability to generate fontmap images and pairing JSON files through a GUI interface
* The ability to automatically load the fonts into the game after manually uploading them

The additions provided here provide:

* A command-line interface to the fontmap generator
* Removes the GUI chooser
* Uploading to ROBLOX if the .ROBLOSECURITY cookie is provided
* Uploading fonts in a range of sizes
* A Lua table as a result, which can be used to automatically load in the list of fonts
* Modifications to the ROBLOX side (FontLibrary) in order to support JSON data being in the decal instead of the image.

**Automatically uploaded files are not entirely compatible with the original FontLibrary because they store JSON data in the Decal asset instead of the Image.**

	Args:
	--upload
	-u
	        Whether or not to upload the images to roblox, 1 or true
	        -u false
	--roblosecurity
	-r
	        Your .ROBLOSECURITY cookie that's used to upload images to your account
	        This is required if you set -u or --upload
	        -r COOKIEGOESHERE
	--local
	-l
	        Whether or not to save to a file
	        -f true
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
	        The name of the image file. FONT, SIZE, and LOC have replacements
	        -i FONT-SIZE-LOC.png
	        -i FONT-image-SIZE.png
	--json
	-j
	        The name of the json file. Same replacements as image
	        -j FONT-SIZE-LOC.json
	        -i FONT-image-SIZE.json
	--table
	-t
	        Whether or not to print a lua table of the names, sizes, decal ids, and their image ids to the output
	        Only works in upload mode.
	        -l true
	--wait
	-w
	        The amount of time to wait between each generation/upload. Suggested 2 for uploads.
	        -w 0
	        -w 5

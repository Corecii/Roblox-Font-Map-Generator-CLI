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

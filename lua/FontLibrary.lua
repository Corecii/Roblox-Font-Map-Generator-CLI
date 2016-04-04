--[[
	Font Library
	A system for managing custom fonts in ROBLOX
	@author Blake Mealey (m0rgoth)
	@since 2015/05/05

	Changes have been made to this file by Corecii Cyr for use in https://github.com/Corecii/Roblox-Font-Map-Generator-CLI
--]]

local class = {}




-- Setup

local market = game:GetService("MarketplaceService")
local web = game:GetService("HttpService")
local mmax = math.max

local function contains(t, item)
	for i, v in next, t do
		if v == item then
			return i
		end
	end
end




-- Fields

class.WHITE_SPACE = {
	TAB = 9,
	NEW_LINE = 10,
	SPACE = 32
}

class.Fonts = {}




-- Methods

--[[
	Retrieves the Font if it has been loaded
	@param fontName: The name of the Font you want
	@param fontSize: The size of the Font you want. If not given, finds the first of the fontName
	@return the Font if it exists, nil if not
--]]
function class:GetFont(fontName, fontSize)
	local list = self.Fonts[fontName:lower():gsub("%W","")]
	if not list then return end
	if fontSize then
		return list[fontSize]
	else
		return ({next(list)})[2]
	end
end

--[[
	Retrieves the font of the same name with the size of:
		The same, if it exists.
		The nearest bigger size, if it exists.
		The nearest smaller size, if it exists.
	This should return good quality fonts that work with scaling
	@param fontName: The name of the Font you want
	@param fontSize: The size of the Font you want. If not given, finds the largest of the fontName
	@return the Font if it exists, nil if not; the scale factor to get the requested size from the font
--]]
function class:GetBestFont(fontName, fontSize)
	local list = self.Fonts[fontName:lower():gsub("%W","")]
	if not list then return end
	local sortedList = {}
	for _, v in next, list do
		sortedList[#sortedList + 1] = v
	end
	table.sort(sortedList, function(a, b)
		return a.size < b.size
	end)
	fontSize = fontSize or math.huge
	local last
	for _, v in next, sortedList do
		last = v
		if v.size >= fontSize then
			break
		end
	end
	return last, last and fontSize/last.size
end

--[[
	Prints out all of the fonts and their sizes that are currently loaded in the module
--]]
function class:ListFonts()
	for fontName, sizes in next, self.Fonts do
		local s = {}
		for _, font in next, sizes do
			table.insert(s, font.size)
		end
		print(fontName.." ("..table.concat(s, ", ")..")")
	end
end

--[[
	Loads a V2 Font into the system.
	@param id: The data or module you want to load into the system
	@return The Font. Will also store it in FontLibrary.Fonts[FontName]
--]]
function class:LoadFont(arg1)
	-- get the data. if it's a module (module is an Instance is a userdata) then require it.
	local data = (type(arg1) == 'userdata' and require(arg1) or arg1)
	local name = data.name:lower():gsub("%W","")
	-- make easy characters table
	data.characters = data.characters or {}
	local chars = data.characters
	if data.data then
		for _, imageInfo in next, data.data do
			for _, charInfo in next, imageInfo.characters do
				chars[charInfo.characterByte] = charInfo
				charInfo.image = "rbxassetid://"..imageInfo.image
			end
		end
	end
	-- make sure that characters has something for bytes 0 to 255 that are at the right index
	data.maxHeight = 0
	local emptyChar = data.characters[32] or {
		width = 0,
		height = 0,
		baseline = 0,
		xOffset = 0,
		advance = 0,
		x = 0,
		y = 0
	}
	for n = 0, 255 do
		if not chars[n] then
			chars[n] = emptyChar
		else
			data.maxHeight = mmax(data.maxHeight, chars[n].height)
		end
	end
	-- set some things
	data.spaceWidth = emptyChar.advance
	-- add it to fonts
	if not class.Fonts[name] then
		class.Fonts[name] = {}
	end
	class.Fonts[name][data.size] = data
end

--[[
	Checks if a string character is a white space character
	@param char: The string of length 1 to check
	@return Whether or not it is a white space character
--]]
function class:IsCharWhiteSpace(char)
	return self:IsByteWhiteSpace(char:byte())
end

--[[
	Checks if the byte representation of a string character is a white space character
	@param char: The string of length 1 to check
	@return Whether or not it is a white space character
--]]
function class:IsByteWhiteSpace(byte)
	return contains(self.WHITE_SPACE, byte)
end

--[[
	Checks if a string character is accepted by the system
	@param char: The string of length 1 to check
	@return Whether or not it is a valid character
--]]
function class:IsCharValid(char)
	return self:IsByteValid(char:byte())
end

--[[
	Checks if a string character is accepted by the system
	@param byte: The byte representation of the string of length 1 to check
	@return Whether or not it is a valid character
--]]
function class:IsByteValid(byte)
	return true -- All bytes are considered valid now.
end

--[[
	Gets the info for a character
	@param font: The Font to check with
	@param char: The string of length 1 to check
	@return The info table for the character in the font
--]]
function class:GetCharInfo(font, char)
	return self:GetByteInfo(font, char:byte())
end

--[[
	Gets the info of a character in a font
	@param font: The Font to check with
	@param byte: The byte representation of the string of length 1 to check
	@return The info table for the character in the font
--]]
function class:GetByteInfo(font, byte)
	return font.characters[byte]
end

--[[
	Gets the advance of a string in a font
	@param font: The Font to check with
	@param str: The string to check
	@param scale: The scale of the font. 1 is normal. Use this when scaling the font up or down.
	@return The advance of the string in pixels
--]]
function class:GetStringAdvance(font, str, scale)
	scale = scale or 1
	local advance = 0
	for i = 1, #str do
		local char = str:sub(i, i)
		local byte = char:byte()
		if self:IsByteValid(byte) and not self:IsByteWhiteSpace(byte) then
			advance = advance + self:GetCharInfo(font, char).advance*scale
		elseif byte == self.WHITE_SPACE.SPACE then
			advance = advance + font.spaceWidth*scale
		elseif byte == self.WHITE_SPACE.TAB then
			advance = advance + font.spaceWidth*4*scale
		end
	end
	return advance
end

return class

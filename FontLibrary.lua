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

local punct = {46, 44, 47, 63, 33, 58, 59, 39, 36, 37, 40, 41, 91, 93, 123, 125, 60, 62, 34, 64, 35, 94, 38, 42, 95, 45, 43, 61, 92, 124, 126, 96}

local function contains(t, item)
	for i, v in next, t do
		if v == item then
			return i
		end
	end
end




-- Fields

class.NUM_START = 48			-- 48 -> 57
class.LOW_ALPHA_START = 97		-- 97 -> 122
class.UP_ALPHA_START = 65		-- 65 -> 90

class.WHITE_SPACE = {
	TAB = 9,
	NEW_LINE = 10,
	SPACE = 32
}

class.DEFAULT_FONTS = {245061598, 245060831, 245063468, 245063630, 245062339, 245062106, 245063849, 245064079, 245064304, 245064473}

class.Fonts = {}




-- Methods

--[[
	Retrieves the Font if it has been loaded
	@param fontName: The name of the Font you want
	@param fontSize: The size of the Font you want. If not given, finds the first of the fontName
	@return the Font if it exists, nil if not
--]]
function class:GetFont(fontName, fontSize)
	local list = self.Fonts[fontName]
	if not list then return end
	if fontSize then
		return self.Fonts[fontName][fontSize]
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
	local list = self.Fonts[fontName]
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
		for i, font in next, sizes do
			table.insert(s, font.size)
		end
		print(fontName.." ("..table.concat(s, ", ")..")")
	end
end

--[[
	Loads all fonts in the FontLibrary.DefaultFonts table
--]]
function class:LoadDefaultFonts()
	for i, fontID in next, self.DEFAULT_FONTS do
		self:LoadFont(fontID)
	end
end

--[[
	Loads a Font into the system.
	@param id: The ROBLOX ID number of the decal you want to load as a Font
	@return The Font. Will also store it in FontLibrary.Fonts.FontName
--]]
function class:LoadFont(decalId)
	local info = market:GetProductInfo(decalId)
	local imageId = 0
	local info2 = info
	while imageId < 1 do
		if info2.AssetTypeId == 1 and info2.Name == info.Name and info2.Creator.Id == info.Creator.Id then
			break
		else
			imageId = imageId - 1
		end
		info2 = market:GetProductInfo(decalId + imageId)
	end
	local data
	assert(
		pcall(function()
			data = web:JSONDecode(info.Description)
		end)
		or pcall(function()
			data = web:JSONDecode(info2.Description)
		end),
		"Could not load the given font: Neither the decal nor the image contain the needed JSON data."
	)
	data.image = "rbxassetid://"..(decalId + imageId)
	if not data.padding then
		data.padding = 0
	end
	if not class.Fonts[data.name] then
		class.Fonts[data.name] = {}
	end
	class.Fonts[data.name][data.size] = data
	if data.uploadVersion == 1 then
		if data.imageWidth and data.imageHeight and data.maxWidth and data.maxHeight and (data.imageWidth > data.maxWidth or data.imageHeight > data.maxHeight) then
			--quick hack to fix scaling for images that are too big
			local scale
			if data.imageWidth > data.imageHeight then
				scale = data.maxWidth/data.imageWidth
			else
				scale = data.maxHeight/data.imageHeight
			end
			data.padding = data.padding*scale
			data.height = data.height*scale
			data.spaceWidth = data.spaceWidth*scale
			data.isScaled = true
			for _, list in next, data.widths do
				for i, v in next, list do
					list[i] = v*scale
				end
			end
		end
	end
	return data
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
	return byte >= self.UP_ALPHA_START and byte < self.UP_ALPHA_START + 26 or
		byte >= self.LOW_ALPHA_START and byte < self.LOW_ALPHA_START + 26 or
		byte >= self.NUM_START and byte < self.NUM_START + 10 or
		contains(punct, byte) ~= nil or
		contains(self.WHITE_SPACE, byte) ~= nil
end

--[[
	Gets the width of a character in a font
	@param font: The Font to check with
	@param char: The string of length 1 to check
	@return The width of the character in pixels
--]]
function class:GetCharWidth(font, char)
	return self:GetByteWidth(font, char:byte())
end

--[[
	Gets the width of a character in a font
	@param font: The Font to check with
	@param byte: The byte representation of the string of length 1 to check
	@return The width of the character in pixels
--]]
function class:GetByteWidth(font, byte)
	if not self:IsByteValid(byte) then return end

	local widthsList = {}
	local offset = 0

	if byte >= self.UP_ALPHA_START and byte < self.UP_ALPHA_START + 26 then
		widthsList = font.widths.upperAlpha
		offset = byte - self.UP_ALPHA_START
	elseif byte >= self.LOW_ALPHA_START and byte < self.LOW_ALPHA_START + 26 then
		widthsList = font.widths.lowerAlpha
		offset = byte - self.LOW_ALPHA_START
	elseif byte >= self.NUM_START and byte < self.NUM_START + 10 then
		widthsList = font.widths.numerical
		offset = byte - self.NUM_START
	else
		widthsList = font.widths.punctuation
		local index = contains(punct, byte)
		if index then
			offset = index - 1
		else
			return nil
		end
	end

	if font.isScaled then
		return widthsList[offset + 1] - 2
	end
	return widthsList[offset + 1]
end

--[[
	Gets the x and y position of the top left position in the sprite sheet of a font for a character
	@param font: The Font to check in
	@param char: The string of length 1 to check
	@return The x and y coords
--]]
function class:GetCharSpriteOffsets(font, char)
	return self:GetByteSpriteOffsets(font, char:byte())
end

--[[
	Gets the x and y position of the top left position in the sprite sheet of a font for a character
	@param font: The Font to check in
	@param byte: The byte representation of the string of length 1 to check
	@return The x and y coords
--]]
function class:GetByteSpriteOffsets(font, byte)
	if not self:IsByteValid(byte) then return end

	local widthsList = {}
	local offset, x, y = 0, 0, font.padding

	if byte >= self.UP_ALPHA_START and byte < self.UP_ALPHA_START + 26 then
		widthsList = font.widths.upperAlpha
		offset = byte - self.UP_ALPHA_START
		y = y + ((font.height + font.padding) * 0)
	elseif byte >= self.LOW_ALPHA_START and byte < self.LOW_ALPHA_START + 26 then
		widthsList = font.widths.lowerAlpha
		offset = byte - self.LOW_ALPHA_START
		y = y + ((font.height + font.padding) * 1)
	elseif byte >= self.NUM_START and byte < self.NUM_START + 10 then
		widthsList = font.widths.numerical
		offset = byte - self.NUM_START
		y = y + ((font.height + font.padding) * 2)
	else
		widthsList = font.widths.punctuation
		offset = contains(punct, byte) - 1
		y = y + ((font.height + font.padding) * 3)
	end

	for i = 0, offset - 1 do
		x = x + widthsList[i + 1]
	end

	if font.isScaled then
		return x + 1, y + 1
	end
	return x, y
end

--[[
	Gets the width of a string in a font
	@param font: The Font to check with
	@param str: The string to check
	@return The width of the string in pixels
--]]
function class:GetStringWidth(font, str)
	local width = 0
	for i = 1, #str do
		local char = str:sub(i, i)
		local byte = char:byte()
		if self:IsByteValid(byte) and not self:IsByteWhiteSpace(byte) then
			width = width + self:GetCharWidth(font, char)
		elseif byte == self.WHITE_SPACE.TAB then
			width = width + font.spaceWidth
		elseif byte == self.WHITE_SPACE.TAB then
			width = width + font.spaceWidth*4
		end
	end
	return width
end

return class

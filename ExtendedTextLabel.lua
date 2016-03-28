--[[
	Extended Text Label
	The TextLabel class but better. Made for use with the FontLibrary module.
	@author Blake Mealey (m0rgoth)
	@since 2015/05/05
--]]

--[[
	Use just like a normal TextLabel.

	To instantiate:

	local ExtendedTextLabel = require(theModule)
	local myLabel = ExtendedTextLabel.new( Optional TextLabel object )

	If the TextLabel is not given in the constructor then the class will instantiate a new one.
	The ExtendedTextLabel object will take all existing properties from the TextLabel object if
	supplied.

	A few properties of TextLabel are unsupported but this class. They are:
		- TextScaled
		- TextStrokeColor
		- TextStrokeColor3
		- TextStrokeTransparency

	Two properties change their data type. They are:
		- Font:		Now takes a string argument. Should be the name of a Font loaded with the
					FontLibrary module.
		- FontSize: Now takes a number argument. Should be a valid size of a Font loaded with
					the FontLibrary module. If the font size is not valid for that font, it
					will use the first valid size it finds.

	I have not extensively tested for bugs and I am sure many exist. If you find any, please
	inform me!
--]]

local class = {}




-- Setup

local FontLibrary = require(script.Parent.FontLibrary)

local function copyTable(t)
	local n = {}
	for k,v in next, t do
		if type(v) == "table" then
			v = copyTable(v)
		end
		n[k] = v
	end
	return n
end




-- Transfer properties/methods/events from the TextLabel to the Frame

local TRANSFERRED = {}

TRANSFERRED.PROPERTIES = {
	"Active", "BackgroundColor3", "BackgroundTransparency", "BorderColor3", "BorderSizePixel", "ClipsDescendants", "Draggable", "Position",
	"Rotation", "SizeConstraint", "Visible", "Archivable", "archivable", "Name"
}

TRANSFERRED.READ_ONLY_PROPERTIES = {
	"AbsolutePosition", "AbsoluteSize"
}

TRANSFERRED.EVENTS = {		-- Essentially the same as TRANSFERRED.READ_ONLY_PROPERTIES
	"DragBegin", "DragStopped", "InputBegan", "InputChanged", "InputEnded", "MouseEnter", "MouseLeave", "MouseMoved", "MouseWheelBackward",
	"MouseWheelForward", "TouchLongPress", "TouchPan", "TouchPinch", "TouchRotate", "TouchSwipe", "TouchTap", "AncestryChanged", "Changed",
	"ChildAdded", "ChildRemoved", "DescendantAdded", "DescendantRemoving", "childAdded"
}

TRANSFERRED.METHODS = {
	"TweenPosition", "TweenSize", "TweenSizeAndPosition", "ClearAllChildren", "FindFirstChild", "GetChildren", "GetFullName", "IsAncestorOf",
	"IsDescendantOf", "findFirstChild", "getChildren", "children", "isDescendantOf", "WaitForChild"
}

local UNSUPPORTED = {}

UNSUPPORTED.PROPERTIES = {
	"TextScaled", "TextStrokeColor", "TextStrokeColor3", "TextStrokeTransparency"
}

local OTHER_PROPERTIES = {
	"Text", "TextColor", "TextColor3", "TextTransparency", "TextWrapped", "Size", "Parent", "ZIndex"
}

for i, prop in next, TRANSFERRED.PROPERTIES do
	class["Set"..prop] = function(self, value)
		self.base[prop] = value
		self.object[prop] = value
	end
	class["Get"..prop] = function(self)
		return self.base[prop]
	end
end

for i, prop in next, TRANSFERRED.READ_ONLY_PROPERTIES do
	class["Get"..prop] = function(self)
		return self.base[prop]
	end
end

for i, event in next, TRANSFERRED.EVENTS do
	class["Get"..event] = function(self)
		return self.base[event]
	end
end

for i, method in next, TRANSFERRED.METHODS do
	class[method] = function(self, ...)
		self.base[method](self.base, ...)
	end
end

for i, prop in next, UNSUPPORTED.PROPERTIES do
	class["Set"..prop] = function(self, value)
		print(("The feature %s is unsupported."):format(prop))
	end
	class["Get"..prop] = function(self)
		print(("The feature %s is unsupported."):format(prop))
	end
end




-- Fields

class.ClassName = "ExtendedTextLabel"
class.className = class.ClassName




-- Override Methods

function class:clone()
	return self:Clone()
end

function class:Clone()
	local newObject = new(self.object:Clone())
	newObject.Font = self.font.name
	newObject.FontSize = self.fontSize
	return newObject
end

function class:isA(className)
	return self:IsA(className)
end

function class:IsA(className)
	return className == self.ClassName
end

function class:remove()
	self:Destroy()
end

function class:Remove()
	self:Destroy()
end

function class:destroy()
	self:Destroy()
end

function class:Destroy()
	self.base:Destroy()
	self.object:Destroy()
end




-- Override property accessors/mutators

function class:SetFont(fontName)
	assert(type(fontName) == "string", "Expected a string for the font name.")
	self.font = FontLibrary:GetFont(fontName, self.fontSize)
	self:SetText(self.object.Text)
end

function class:GetFont()
	return self.font.name
end

function class:SetFontSize(size)
	assert(type(size) == "number", "Expected a number for the font size.")
	self.fontSize = size
	if self.font then
		self:SetFont(self.font.name)
	end
end

function class:GetFontSize()
	return self.fontSize
end

function class:SetText(text)
	text = tostring(text)
	self.object.Text = text
	self.base:ClearAllChildren()

	if not self.font or not self.fontSize then return end

	local line

	local guiX, guiY = 0, 0
	local guiWidth = 0

	for i = 1, #text do
		if guiX == 0 then
			line = Instance.new("Frame", self.base)
			line.Name = "Line"..guiY
			line.BackgroundTransparency = 1
			line.ZIndex = self.base.ZIndex
		end

		local char = text:sub(i, i)
		local byte = char:byte()

		local valid, x, y, width = true, 0, 0, self.font.spaceWidth

		if not FontLibrary:IsByteWhiteSpace(byte) then
			valid = FontLibrary:IsByteValid(byte)
			if valid then
				width = FontLibrary:GetByteWidth(self.font, byte)
				x, y = FontLibrary:GetByteSpriteOffsets(self.font, byte)

				local label = Instance.new("ImageLabel")
				label.Name = char
				label.BackgroundTransparency = 1
				label.Size = UDim2.new(0, width, 0, self.font.height)
				label.Position = UDim2.new(0, guiX, 0, 0)
				label.Image = self.font.image
				label.ImageRectSize = Vector2.new(width, self.font.height)
				label.ImageRectOffset = Vector2.new(x, y)
				label.ImageTransparency = self.object.TextTransparency
				label.ImageColor3 = self.object.TextColor3
				label.ZIndex = self.base.ZIndex

				--[[if self.object.TextStrokeTransparency < 1 then
					local stroke = label:Clone()
					stroke.Name = char.."Stroke"
					stroke.Size = stroke.Size + UDim2.new(0, 4, 0, 4)
					stroke.Position = stroke.Position - UDim2.new(0, 2, 0, 2)
					stroke.ImageTransparency = self.object.TextStrokeTransparency
					stroke.ImageColor3 = self.object.TextStrokeColor3
					stroke.ZIndex = self.base.ZIndex
					stroke.Parent = line
				end]]

				label.Parent = line
			end

			line.Size = UDim2.new(0, guiX + width, 0, self.font.height)
		else
			width = byte == FontLibrary.WHITE_SPACE.NEW_LINE and 0 or
				byte == FontLibrary.WHITE_SPACE.TAB and self.font.spaceWidth*4 or
				byte == FontLibrary.WHITE_SPACE.SPACE and self.font.spaceWidth
		end

		if guiX > guiWidth then
			guiWidth = guiX + width
		end

		local nextSpace = text:find(" ", i)
		local nextWord = nextSpace and text:sub(nextSpace, text:find(" ", nextSpace + 1)) or nil
		if byte == FontLibrary.WHITE_SPACE.NEW_LINE then
			guiY = guiY + 1
			guiX = 0
		elseif byte == FontLibrary.WHITE_SPACE.TAB then
			if self.object.TextWrapped and nextWord then
				local nextWidth = FontLibrary:GetStringWidth(self.font, nextWord)
				if guiX + self.font.spaceWidth*4 + nextWidth > self.base.AbsoluteSize.X then
					guiY = guiY + 1
					guiX = 0
				else
					guiX = guiX + width
				end
			else
				guiX = guiX + width
			end
		elseif byte == FontLibrary.WHITE_SPACE.SPACE then
			if self.object.TextWrapped and nextWord then
				local nextWidth = FontLibrary:GetStringWidth(self.font, nextWord)
				if guiX + self.font.spaceWidth + nextWidth > self.base.AbsoluteSize.X then
					guiY = guiY + 1
					guiX = 0
				else
					guiX = guiX + width
				end
			else
				guiX = guiX + width
			end
		elseif valid then
			guiX = guiX + width
		end
	end

	for i, line in next, self.base:GetChildren() do
		local xPos, yPos

		if self.object.TextXAlignment == Enum.TextXAlignment.Left then
			xPos = Vector2.new(0, 0)
		elseif self.object.TextXAlignment == Enum.TextXAlignment.Center then
			xPos = Vector2.new(.5, -line.Size.X.Offset/2)
		elseif self.object.TextXAlignment == Enum.TextXAlignment.Right then
			xPos = Vector2.new(1, -line.Size.X.Offset)
		end

		local total, totalUsed = self.base.AbsoluteSize.Y, (guiY + 1)*self.font.height
		if self.object.TextYAlignment == Enum.TextYAlignment.Top then
			yPos = Vector2.new(0, self.font.height*(i - 1))
		elseif self.object.TextYAlignment == Enum.TextYAlignment.Center then
			yPos = Vector2.new(0, (total - totalUsed)/2 + self.font.height*(i - 1))
		elseif self.object.TextYAlignment == Enum.TextYAlignment.Bottom then
			yPos = Vector2.new(0, (total - totalUsed) + self.font.height*(i - 1))
		end

		line.Position = UDim2.new(xPos.X, xPos.Y, yPos.X, yPos.Y)
	end

	self.textBounds = Vector2.new(guiWidth, (guiY + 1)*self.font.height)

	return self.base
end

function class:GetText()
	return self.object.Text
end

function class:GetTextBounds()
	return self.textBounds or Vector2.new()
end

function class:SetTextColor(color)
	self:SetTextColor3(color.Color)
end

function class:SetTextColor3(color)
	self.object.TextColor3 = color
	for i, line in next, self.base:GetChildren() do
		for j, char in next, line:GetChildren() do
			char.ImageColor3 = color
		end
	end
end

function class:GetTextFits()
	local bounds = self:GetTextBounds()
	return bounds.X <= self.base.AbsoluteSize.X and bounds.Y <= self.base.AbsoluteSize.Y
end

function class:SetTextTransparency(trans)
	self.object.TextTransparency = trans
	for i, line in next, self.base:GetChildren() do
		for j, char in next, line:GetChildren() do
			char.ImageTransparency = trans
		end
	end
end

function class:SetTransparency(trans)
	self.object.BackgroundTransparency = trans
	self:SetTextTransparency(trans)
end

function class:SetTextWrapped(state)
	self.object.TextWrapped = state
	self:SetText(self.object.Text)
end

function class:SetSize(size)
	self.object.Size = size
	self.base.Size = size
	self:SetText(self.object.Text)
end

function class:GetSize()
	return self.base.Size
end

function class:SetParent(obj)
	self.base.Parent = obj
	self.object.Parent = nil
	delay(.001, function()
		self:SetText(self:GetText())
	end)
end

function class:GetParent()
	return self.base.Parent
end

function class:SetZIndex(z)
	self.base.ZIndex = z
	self.object.ZIndex = z
	for i, line in next, self.base:GetChildren() do
		line.ZIndex = z
		for j, char in next, line:GetChildren() do
			char.ZIndex = z
		end
	end
end




-- Finish setup

function new(object)
	local module = {}
	local mt = {}

	object = object or Instance.new("TextLabel")

	local classCopy = copyTable(class)
	classCopy.object = object

	classCopy.base = Instance.new("Frame")

	mt.__metatable = true
	mt.__index = function(t, k)
		if classCopy[k] then
			return classCopy[k]
		elseif object[k] then
			if classCopy["Get"..k] then
				return classCopy["Get"..k](classCopy)
			else
				return object[k]
			end
		end
	end
	mt.__newindex = function(t, k, v)
		if classCopy["Set"..k] then
			classCopy["Set"..k](classCopy, v)
		else
			if (classCopy[k]) then
				error("Cannot change FontLibrary API", 0)
			else
				object[k] = v
			end
		end
	end
	mt.__eq = function(t, other)
		return (t == other or object == other)
	end
	setmetatable(module, mt)

	for i, prop in next, TRANSFERRED.PROPERTIES do
		module[prop] = object[prop]
	end

	for i, prop in next, OTHER_PROPERTIES do
		module[prop] = object[prop]
	end

	module.Parent = object.Parent

	return module
end

return {new = new}

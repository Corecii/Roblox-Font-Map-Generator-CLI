
-- Don't run on the client or the server, only studio
if game:FindService("NetworkServer") or game:GetService("Players").LocalPlayer then
	return
end

local FontLibrary = require(script.FontLibrary)
local ExtendedTextLabel = require(script.ExtendedTextLabelModifiable)

local n
local desc
desc = function(o, f)
	for _, v in next, o:GetChildren() do
		if pcall(function() n = v.Name end) then
			f(v)
			desc(v, f)
		end
	end
end

local active = {}
local doItem = function(item)
	if item.Name == "ExtendedFontName" and item.Parent and not active[item.Parent] then
		active[item.Parent] = ExtendedTextLabel.new(item.Parent)
	end
end

desc(game, doItem)

game.DescendantAdded:connect(function(v)
	if not pcall(function() n = v.Name end) then
		return
	end
	doItem(v)
end)

game.ItemChanged:connect(function(item, prop)
	if not pcall(function() n = item.Name end) then
		return
	end
	if prop == "Name" then
		wait()
		if item.Name == "ExtendedFontName" then
			doItem(item.Parent)
		end
	end
end)

game.DescendantRemoving:connect(function(d)
	if not pcall(function() n = d.Name end) then
		return
	end
	if active[d] then
		active[d]:Deactivate(true)
		active[d] = nil
	end
end)

_G.loadFonts = function()
	for _, v in next, game:GetService("Selection"):Get() do
		if v:IsA("ModuleScript") then
			local font = require(v)
			FontLibrary:LoadFont(font)
		end
	end
	for _, v in next, active do
		v.Font = v.Font
	end
end

print("Select font modules and use _G.loadFonts() to load them.")

game.Close:connect(function()
	for _, v in next, active do
		v:Deactivate(true)
	end
end)

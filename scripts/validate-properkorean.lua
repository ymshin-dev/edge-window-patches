-- Runs the actual patched Game:set_language against a small LÖVE filesystem
-- stub. Rendering/device checks remain separate from these Lua runtime checks.
local assets = assert(arg[1], "usage: luajit validate-properkorean.lua ASSET_DIRECTORY")
Object = {extend = function() return {} end}
love = {
    filesystem = {
        read = function(path)
            local file = io.open(assets .. "/" .. path, "rb")
            if not file then return nil end
            local contents = file:read("*a")
            file:close()
            return contents
        end,
        getInfo = function(path)
            local file = io.open(assets .. "/" .. path, "rb")
            if not file then return nil end
            file:close()
            return {type = "file"}
        end,
    },
    graphics = {newFont = function(path, scale) return {path = path, scale = scale} end},
}
assert(loadfile(assets .. "/game.lua"))()
-- Load the actual localization parser and appended helpers, not a test copy.
assert(loadfile(assets .. "/functions/misc_functions.lua"))()
G = Game
G.TILESIZE = 20
G.SETTINGS = {language = "en-us"}
G.F_NO_ACHIEVEMENTS = true
G.CHALLENGES = {
    {id = "c_cruelty_1", rules = {custom = {
        {id = "no_reward_specific", value = "Small"},
        {id = "no_reward_specific", value = "Big"},
    }}},
}
local init_calls = 0
local actual_init_localization = init_localization
function init_localization()
    init_calls = init_calls + 1
    actual_init_localization()
end
G:set_language()
local english = assert(G.localization.misc.dictionary.b_play_cap)
local original_english = assert(loadstring(love.filesystem.read("localization/en-us.lua")))()
assert(english == original_english.misc.dictionary.b_play_cap, "English startup changed")
local english_joker = G.localization.descriptions.Joker.j_joker.name

-- Add mobile-only keys and extra lines to the real base-language input.
local normal_read = love.filesystem.read
love.filesystem.read = function(path)
    local text = normal_read(path)
    if path == "localization/ko.lua" then
        return "local base = (function() " .. text .. " end)(); " ..
            "base.misc.dictionary.mobile_only_test = 'mobile'; " ..
            "base.descriptions.Joker.j_joker.text[9] = 'stale'; return base"
    end
    return text
end
G.SETTINGS.language = "ko"
G:set_language()
local translated = assert(loadstring(normal_read("properkorean/ko.lua")))()
assert(G.localization.descriptions.Joker.j_joker.name == translated.descriptions.Joker.j_joker.name)
assert(G.localization.descriptions.Joker.j_joker.text[9] == nil, "stale description line survived")
assert(G.localization.misc.dictionary.mobile_only_test == "mobile", "Android-only key lost")
assert(G.LANG.font.FONT.path == "resources/fonts/NotoSansKR-Bold.ttf", "wrong Korean font")
assert(G.CHALLENGES[1].rules.custom[1].value == "스몰")
assert(G.CHALLENGES[1].rules.custom[2].value == "빅")
assert(G.F_NO_ACHIEVEMENTS == true, "translation changed achievement policy")
assert(get_josa_for_rank("잭") == "을")
assert(get_josa_for_rank("에이스", true) == "가")
assert(get_josa_for_poker_hand("트리플") == "을")
assert(type(korean_credits) == "function")
assert(#G.localization.descriptions.Joker.j_joker.text_parsed > 0)
local ox_text = localize({type = "raw_descriptions", key = "bl_ox", set = "Blind", vars = {"트리플"}})
assert(table.concat(ox_text):find("트리플을", 1, true), "Ox grammar not applied by actual localize")
assert(init_calls == 2, "localization initialized recursively")

G.SETTINGS.language = "en-us"
G:set_language()
assert(G.localization.misc.dictionary.b_play_cap == english)
assert(G.localization.descriptions.Joker.j_joker.name == english_joker)
assert(G.localization.misc.dictionary.mobile_only_test == nil)
assert(G.CHALLENGES[1].rules.custom[1].value == "Small")
assert(G.CHALLENGES[1].rules.custom[2].value == "Big")
G.SETTINGS.language = "ko"
G:set_language()
assert(init_calls == 4)
assert(G.localization.descriptions.Joker.j_joker.name == translated.descriptions.Joker.j_joker.name)
print("PASS: actual Game:set_language, Korean/English switching, dictionary merge, list replacement, font path, grammar, achievement policy")

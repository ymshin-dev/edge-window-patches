-- Android adaptation of DVRP's ProperKorean v0.7.1 startup routine.
-- The font is replaced in the APK at its normal path; no Mods directory needed.
local function merge_properkorean(target, source)
    for key, value in pairs(source) do
        -- Dictionaries merge so Android-only strings survive. Lists replace so
        -- shorter descriptions do not retain stale lines from the original.
        if type(value) == "table" and next(value) ~= nil and value[1] == nil then
            if type(target[key]) ~= "table" then target[key] = {} end
            merge_properkorean(target[key], value)
        else
            target[key] = value
        end
    end
end

function apply_properkorean_android()
    local is_korean = G.LANG.key == "ko"
    if is_korean then
        local translation = assert(loadstring(assert(
            love.filesystem.read("properkorean/ko.lua")
        )))()
        merge_properkorean(G.localization, translation)
    end

    -- Game:set_language also runs when switching languages in settings.
    for _, challenge in pairs(G.CHALLENGES or {}) do
        if challenge.id == "c_cruelty_1" then
            for _, rule in ipairs(challenge.rules.custom) do
                if rule.id == "no_reward_specific" then
                    if rule.value == "Small" or rule.value == "스몰" then
                        rule.value = is_korean and "스몰" or "Small"
                    elseif rule.value == "Big" or rule.value == "빅" then
                        rule.value = is_korean and "빅" or "Big"
                    end
                end
            end
        end
    end
end

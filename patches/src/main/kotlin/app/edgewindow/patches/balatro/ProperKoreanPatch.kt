package app.edgewindow.patches.balatro

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch

private const val RESOURCE_ROOT = "balatro/properkorean/"
private const val MARKER = "-- Edge Window Patches: ProperKorean Android v0.7.1"
private object ProperKoreanResources

private fun bundled(name: String): ByteArray =
    ProperKoreanResources::class.java.classLoader.getResourceAsStream(RESOURCE_ROOT + name)
        ?.use { it.readBytes() }
        ?: throw PatchException("Missing bundled ProperKorean resource: $name")

internal data class LuaEdit(
    val target: String,
    val pattern: String,
    val position: String,
    val payload: String,
    val expectedMatches: Int,
) {
    fun apply(source: String): String {
        val matcher = Regex(pattern.split('*').joinToString(".*") { Regex.escape(it) })
        val lines = source.split('\n')
        val count = lines.count { matcher.matches(it.trim()) }
        if (count != expectedMatches) {
            throw PatchException(
                "Unsupported Balatro Lua layout in $target: expected $expectedMatches " +
                    "matches, found $count for $pattern. Use the supported 1.8 APK.",
            )
        }
        return lines.joinToString("\n") { line ->
            if (!matcher.matches(line.trim())) line else {
                val indent = line.takeWhile { it == ' ' || it == '\t' }
                val insertion = payload.lines().joinToString("\n") { indent + it }
                when (position) {
                    "before" -> "$insertion\n$line"
                    "after" -> "$line\n$insertion"
                    "at" -> insertion
                    else -> throw PatchException("Invalid ProperKorean edit position: $position")
                }
            }
        }
    }
}

@Suppress("unused")
val properKoreanPatch = rawResourcePatch(
    name = "Proper Korean translation",
    description = "Applies DVRP's ProperKorean (옳은 한글) translation, font, grammar and UI fixes to Balatro.",
    default = false,
) {
    compatibleWith(
        *arrayOf("com.playstack.balatro.android", "com.playstack.balatro.android.morphe").map {
            Compatibility(
                packageName = it,
                name = "Balatro",
                apkFileType = ApkFileType.APK_REQUIRED,
                targets = listOf(AppTarget(version = "1.8")),
            )
        }.toTypedArray(),
    )

    execute {
        val context = this
        val targets = properKoreanEdits.map { it.target }.distinct()
        // Validate and prepare all edits before writing any game file. Patching
        // exact lines preserves other Morphe changes in the supplied APK.
        val sources = targets.associateWith { target ->
            val file = context["assets/$target"]
            if (!file.isFile) throw PatchException("Missing Balatro Lua asset: $target")
            file.readText().replace("\r\n", "\n")
        }.toMutableMap()
        if (sources.values.any { MARKER in it || "function apply_properkorean(" in it }) {
            throw PatchException("ProperKorean is already installed. Start from an APK without this translation patch.")
        }
        properKoreanEdits.forEach { edit ->
            sources[edit.target] = edit.apply(sources.getValue(edit.target))
        }
        val helperTarget = "functions/misc_functions.lua"
        sources[helperTarget] = sources.getValue(helperTarget) + "\n$MARKER\n" +
            bundled("android.lua").toString(Charsets.UTF_8) + "\n" +
            bundled("helpers.lua").toString(Charsets.UTF_8)

        val additions = mapOf(
            "assets/properkorean/ko.lua" to bundled("ko.lua"),
            "assets/resources/fonts/NotoSansKR-Bold.ttf" to bundled("NotoSansKR-Bold.ttf"),
            "assets/properkorean/NOTICE.md" to bundled("NOTICE.md"),
            "assets/properkorean/OFL.txt" to bundled("OFL.txt"),
        )
        sources.forEach { (target, source) -> context["assets/$target"].writeText(source) }
        additions.forEach { (path, bytes) ->
            context[path].apply { parentFile.mkdirs() }.writeBytes(bytes)
        }
    }
}

package dev.taboo.taboo.util

object ParseBytes {

    @JvmStatic
    fun parseBytes(bytes: Double): String {
        val converted: String
        val kb = bytes / 1024
        val mb = kb / 1024
        val gb = mb / 1024
        converted = if (gb > 0) {
            "$gb GB"
        } else if (mb > 0) {
            "$mb MB"
        } else {
            "$kb KB"
        }
        return converted
    }

}
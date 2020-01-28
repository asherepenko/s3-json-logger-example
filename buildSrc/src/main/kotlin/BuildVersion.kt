import java.io.File
import java.lang.IllegalArgumentException

data class BuildVersion(
    val major: Int = 1,
    val minor: Int = 0,
    val patch: Int = 0,
    val build: Int = 0
) {
    companion object {

        private val PATTERN = "(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d)".toRegex()

        @JvmStatic
        fun parse(versionText: String): BuildVersion {
            val result = PATTERN.matchEntire(versionText)
                ?: throw IllegalArgumentException("Unable to parse build version: $versionText")
            return BuildVersion(
                major = result.groupValues[1].toInt(),
                minor = result.groupValues[2].toInt(),
                patch = result.groupValues[3].toInt(),
                build = result.groupValues[4].toInt()
            )
        }

        @JvmStatic
        fun parse(versionFile: File): BuildVersion =
            if (versionFile.exists() && versionFile.canRead()) {
                parse(versionFile.readText())
            } else {
                throw IllegalArgumentException("Unable to read version file: ${versionFile.path}")
            }
    }


    val versionCode: Int
        get() = major * 10000 + minor * 1000 + patch * 100 + build

    val versionName: String
        get() = "$major.$minor.$patch.$build"
}

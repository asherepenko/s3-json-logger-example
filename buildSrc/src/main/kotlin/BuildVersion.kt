data class BuildVersion(
    val major: Int = 1,
    val minor: Int = 0,
    val patch: Int = 0,
    val build: Int = 1
) {
    val versionCode: Int
        get() = major * 10000 + minor * 1000 + patch * 100 + build

    val versionName: String
        get() = "$major.$minor.$patch"
}

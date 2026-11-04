package ir.divarfiling.mobile.feature.contentstudio.canvas

object PcsManifestValidator {
    const val EXPECTED_MANIFEST_VERSION_PREFIX = "pcs-manifest-"
    const val MIN_SCENE_SCHEMA = 2
    const val MIN_DYNAMIC_SCHEMA = 1

    fun validateManifestVersion(version: String): String? {
        if (!version.startsWith(EXPECTED_MANIFEST_VERSION_PREFIX)) {
            return "نسخه Manifest نامعتبر است"
        }
        return null
    }

    fun validateScene(scene: PcsSceneDocument?): String? {
        if (scene == null) return "صحنه Manifest خالی است"
        if (scene.schemaVersion < MIN_SCENE_SCHEMA) {
            return "نسخه Schema صحنه پشتیبانی نمی‌شود"
        }
        if (scene.layers.isEmpty()) return "صحنه بدون لایه است"
        return null
    }
}

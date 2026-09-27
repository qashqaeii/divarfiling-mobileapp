package ir.divarfiling.mobile.core.avatar

import ir.divarfiling.mobile.core.network.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarPickerLogicTest {
    @Test
    fun seed_uses_saved_preset() {
        val user = UserDto(id = 1, fullName = "u", avatarKey = "avatar_07")
        assertEquals("avatar_07", AvatarPickerLogic.seedKey(user))
    }

    @Test
    fun seed_defaults_when_custom_photo() {
        val user = UserDto(
            id = 1,
            fullName = "u",
            hasCustomAvatar = true,
            avatarKey = "avatar_03",
            avatarUrl = "https://x/y.jpg",
        )
        assertEquals(AvatarCatalog.DEFAULT_KEY, AvatarPickerLogic.seedKey(user))
    }

    @Test
    fun can_save_when_preset_changed() {
        val user = UserDto(id = 1, fullName = "u", avatarKey = "avatar_01")
        assertTrue(AvatarPickerLogic.canSave(user, "avatar_05", pickerTouched = true))
        assertFalse(AvatarPickerLogic.canSave(user, "avatar_01", pickerTouched = true))
    }

    @Test
    fun can_save_custom_avatar_after_touch() {
        val user = UserDto(
            id = 1,
            fullName = "u",
            hasCustomAvatar = true,
            avatarUrl = "https://x/y.jpg",
        )
        assertFalse(AvatarPickerLogic.canSave(user, "avatar_01", pickerTouched = false))
        assertTrue(AvatarPickerLogic.canSave(user, "avatar_01", pickerTouched = true))
    }
}

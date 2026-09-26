package ir.divarfiling.mobile.core.avatar

import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarResolverTest {
    @Test
    fun contact_without_photo_uses_default_avatar() {
        val resolved = AvatarResolver.resolveContact(ContactDto(id = 1, fullName = "علی"))
        assertTrue(resolved.usePreset)
        assertEquals(AvatarCatalog.DEFAULT_KEY, resolved.presetKey)
    }

    @Test
    fun contact_with_photo_uses_contact_photo() {
        val resolved = AvatarResolver.resolveContact(
            ContactDto(id = 1, fullName = "علی", photoUrl = "https://example.com/p.jpg"),
        )
        assertEquals("https://example.com/p.jpg", resolved.remoteUrl)
        assertFalse(resolved.usePreset)
    }

    @Test
    fun user_selected_avatar_is_persisted() {
        val resolved = AvatarResolver.resolveUser(
            UserDto(id = 1, fullName = "کاربر", avatarKey = "avatar_05"),
        )
        assertTrue(resolved.usePreset)
        assertEquals("avatar_05", resolved.presetKey)
    }

    @Test
    fun missing_avatar_asset_falls_back_to_avatar_01() {
        val drawable = AvatarCatalog.drawableForKey("avatar_99")
        val defaultDrawable = AvatarCatalog.drawableForKey(AvatarCatalog.DEFAULT_KEY)
        assertEquals(defaultDrawable, drawable)
    }

    @Test
    fun existing_profile_photo_is_not_overwritten() {
        val resolved = AvatarResolver.resolveUser(
            UserDto(
                id = 1,
                fullName = "کاربر",
                avatarUrl = "https://example.com/me.jpg",
                hasCustomAvatar = true,
                avatarKey = "avatar_02",
            ),
        )
        assertEquals("https://example.com/me.jpg", resolved.remoteUrl)
        assertFalse(resolved.usePreset)
    }
}

package ir.divarfiling.mobile.core.fcm

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmEntryPoint {
    fun fcmTokenSync(): FcmTokenSync
}

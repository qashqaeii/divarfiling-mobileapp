package ir.divarfiling.mobile.core.sync

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ir.divarfiling.mobile.core.datastore.SessionStore

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerSessionEntryPoint {
    fun sessionStore(): SessionStore
    fun licenseRepository(): ir.divarfiling.mobile.data.repository.LicenseRepository

    companion object {
        fun sessionStore(context: Context): SessionStore =
            EntryPointAccessors.fromApplication(context, WorkerSessionEntryPoint::class.java)
                .sessionStore()
    }
}

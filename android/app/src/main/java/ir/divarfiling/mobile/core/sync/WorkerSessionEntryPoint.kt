package ir.divarfiling.mobile.core.sync

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.divarfiling.mobile.core.datastore.SessionStore

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerSessionEntryPoint {
    fun sessionStore(): SessionStore
}

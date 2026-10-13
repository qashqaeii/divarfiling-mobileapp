package ir.divarfiling.mobile.feature.postcall

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import ir.divarfiling.mobile.feature.postcall.presentation.PostCallOverlayController

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PostCallEntryPoint {
    fun postCallBootstrap(): PostCallBootstrap
    fun postCallController(): PostCallController
    fun postCallEventHub(): PostCallEventHub
    fun postCallPermissionManager(): PostCallPermissionManager
    fun postCallOverlayController(): PostCallOverlayController
    fun crmRepository(): CrmRepository
}

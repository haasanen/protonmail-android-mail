package ch.protonmail.android.maillabel.domain.usecase

import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@Deprecated("Broken to allow migrating to using ViewMode from mail-label. to be fixed in next commit.")
class ObserveCurrentViewMode @Inject constructor(
//    private val observeMailSettings: ObserveMailSettings,
    private val observeMessageOnlyLabelIds: ObserveMessageOnlyLabelIds
) {

    operator fun invoke(userId: UserId, selectedLabelId: LabelId): Flow<ViewMode> =
        observeMessageOnlyLabelIds(userId).flatMapLatest { messageOnlyLabelIds ->
            if (selectedLabelId in messageOnlyLabelIds) {
                flowOf(ViewMode.NoConversationGrouping)
            } else invoke(userId)
        }

    @Suppress("NotImplementedDeclaration")
    operator fun invoke(userId: UserId): Flow<ViewMode> =
        TODO("Impl broken by commit to allow migrating ViewMode. Fixed in next commit")
//        observeMailSettings(userId)
//        .filterNotNull()
//        .mapLatest { it.viewMode?.enum ?: DefaultViewMode }
//        .distinctUntilChanged()

    companion object {

        val DefaultViewMode = ViewMode.NoConversationGrouping
    }
}

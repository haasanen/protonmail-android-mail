package ch.protonmail.android.maillabel.domain.usecase

import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailsettings.domain.usecase.ObserveMailSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ViewMode
import javax.inject.Inject

class ObserveCurrentViewMode @Inject constructor(
    private val observeMailSettings: ObserveMailSettings,
    private val observeMessageOnlyLabelIds: ObserveMessageOnlyLabelIds
) {

    operator fun invoke(userId: UserId, selectedLabelId: LabelId): Flow<ViewMode> =
        observeMessageOnlyLabelIds(userId).flatMapLatest { messageOnlyLabelIds ->
            if (selectedLabelId in messageOnlyLabelIds) {
                flowOf(ViewMode.NoConversationGrouping)
            } else invoke(userId)
        }

    operator fun invoke(userId: UserId): Flow<ViewMode> = observeMailSettings(userId)
        .filterNotNull()
        .mapLatest { it.viewMode?.enum ?: DefaultViewMode }
        .distinctUntilChanged()

    companion object {

        val DefaultViewMode = ViewMode.NoConversationGrouping
    }
}

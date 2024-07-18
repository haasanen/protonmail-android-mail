/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.maillabel.data.usecase

import ch.protonmail.android.maillabel.data.local.LabelDataSource
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId
import uniffi.proton_mail_common.LocalLabelId
import javax.inject.Inject

class FindLocalLabelId @Inject constructor(
    private val labelDataSource: LabelDataSource
) {
    suspend operator fun invoke(userId: UserId, systemLabelId: LabelId): LocalLabelId? {
        labelDataSource.observeSystemLabels(userId).filter { it.isNotEmpty() }.first().let { labels ->
            return labels.find { it.rid == systemLabelId.id }?.id

        }
    }
}

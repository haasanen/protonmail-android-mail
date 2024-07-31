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

package ch.protonmail.android.maillabel.domain.usecase

import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@Deprecated(
    """
   With the introduction of dynamic system labels, the behavior of this use case changed
    and it's now returning the full list of system labels as exposed by rust
    with no filtering on the "exclusiveness" anymore. 
    
    **This will break any functionality that relied on such list being filtered, such as move-to etc.**
   Those functionalities will need adaptation to work with rust anyways thus leaving this change for then.
   This should be updated (likely, dropped) as those features are implemented)
"""
)
class ObserveExclusiveMailLabels @Inject constructor(
    private val exclusiveDestinationMailFolders: ObserveExclusiveDestinationMailLabels
) {

    operator fun invoke(userId: UserId) = exclusiveDestinationMailFolders(userId)

}

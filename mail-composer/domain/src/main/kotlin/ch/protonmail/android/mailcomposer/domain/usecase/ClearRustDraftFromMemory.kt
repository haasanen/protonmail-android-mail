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

package ch.protonmail.android.mailcomposer.domain.usecase

import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import javax.inject.Inject

/**
 *
 * Needed due to commit c221f45b9 (Update rust draft data source DI bindings to make it Singleton)
 *
 * This is a tradeoff to allow the Composer Screen to split functionality between multiple view models.
 *
 * Due to the setup of the rust library, objects (ie. the rust Draft) are held in memory
 * by clients. In this case, the "Draft Data Source" is the one holding an instance of the
 * rust Draft; The instance of draft data source is bound to ComposerViewModel
 * and allows to operate on the same "draft" without passing any identifiers (as by design,
 * there can only be one draft opened at a given time).
 *
 * This also creates a big limitation as only the ComposerViewModel can access the draft instance,
 * meaning any and all logic needs to go through the composer VM (as other VMs will be injected with another
 * instance of RustDraftDataSource).
 *
 * Performing this "clear from memory" helps guaranteeing that next time a composer is opened there will
 * be no info of the previous drafts left laying around
 *
 */
class ClearRustDraftFromMemory @Inject constructor(
    private val draftRepository: DraftRepository
) {

    operator fun invoke() = draftRepository.clearDraftFromMemory()
}

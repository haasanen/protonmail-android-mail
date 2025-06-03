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

package ch.protonmail.android.mailcomposer.presentation.mapper.modifications

import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailattachments.domain.model.AttachmentState
import ch.protonmail.android.mailattachments.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.AttachmentsStateModification
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentMetadataUiModelMapper
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentGroupUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.NO_ATTACHMENT_LIMIT
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class AttachmentsStateModificationTest(
    @Suppress("unused") private val testName: String,
    private val initialState: ComposerState.Attachments,
    private val modification: AttachmentsStateModification.ListUpdated,
    private val expectedState: ComposerState.Attachments
) {

    @Test
    fun `should apply the modification`() {
        val updatedState = modification.apply(initialState)
        assertEquals(expectedState, updatedState)
    }

    companion object {

        private val initialState = ComposerState.Attachments.initial()

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "empty attachments list",
                initialState,
                AttachmentsStateModification.ListUpdated(emptyList()),
                ComposerState.Attachments(
                    uiModel = AttachmentGroupUiModel(limit = NO_ATTACHMENT_LIMIT, attachments = emptyList())
                )
            ),
            arrayOf(
                "single attachment list",
                initialState,
                AttachmentsStateModification.ListUpdated(
                    listOf(
                        AttachmentMetadataWithState(AttachmentMetadataSamples.Invoice, AttachmentState.Uploaded)
                    )
                ),
                ComposerState.Attachments(
                    uiModel = AttachmentGroupUiModel(
                        limit = NO_ATTACHMENT_LIMIT,
                        attachments = listOf(
                            AttachmentMetadataUiModelMapper().toUiModel(
                                attachmentMetadata = AttachmentMetadataSamples.Invoice,
                                isDeletable = true,
                                status = AttachmentState.Uploaded
                            )
                        )
                    )
                )
            ),
            arrayOf(
                "multiple attachments list",
                initialState,
                AttachmentsStateModification.ListUpdated(
                    listOf(
                        AttachmentMetadataWithState(AttachmentMetadataSamples.Invoice, AttachmentState.Uploaded),
                        AttachmentMetadataWithState(AttachmentMetadataSamples.Document, AttachmentState.Uploaded)
                    )
                ),
                ComposerState.Attachments(
                    uiModel = AttachmentGroupUiModel(
                        limit = NO_ATTACHMENT_LIMIT,
                        attachments = listOf(
                            AttachmentMetadataUiModelMapper().toUiModel(
                                attachmentMetadata = AttachmentMetadataSamples.Invoice,
                                isDeletable = true,
                                status = AttachmentState.Uploaded
                            ),
                            AttachmentMetadataUiModelMapper().toUiModel(
                                attachmentMetadata = AttachmentMetadataSamples.Document,
                                isDeletable = true,
                                status = AttachmentState.Uploaded
                            )
                        )
                    )
                )
            )
        )
    }
}

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

package ch.protonmail.android.maillabel.domain.model

import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.maillabel.domain.model.SystemLabelId.Companion.unmodifiableByUserList
import me.proton.core.label.domain.entity.LabelId

enum class SystemLabelId(val labelId: LabelId) {
    /** Displayed. */
    Inbox(LabelId("0")),

    /**
     * Not displayed.
     * All the draft mails have this label.
     * This is necessary because a Draft could have been moved into another folder than `Drafts`.
     */
    AllDrafts(LabelId("1")),

    /**
     * Not displayed.
     * All the sent mails have this label.
     * This is necessary because a Sent message could have been moved into another folder than `Sent.
     */
    AllSent(LabelId("2")),

    /* Displayed. */
    Trash(LabelId("3")),

    /* Displayed. */
    Spam(LabelId("4")),

    /* Displayed. */
    AllMail(LabelId("5")),

    /* Displayed. */
    Archive(LabelId("6")),

    /* Displayed. */
    Sent(LabelId("7")),

    /* Displayed. */
    Drafts(LabelId("8")),

    /* Displayed. */
    Outbox(LabelId("9")),

    /* Displayed. */
    Starred(LabelId("10")),

    /* Not Displayed. */
    AllScheduled(LabelId("12")),

    /* Not Displayed. */
    AlmostAllMail(LabelId("15")),

    /* Not Displayed. */
    Snoozed(LabelId("16"));

    companion object {

        private val map = entries.associateBy { stringOf(it) }

        @Deprecated("Replaced by dynamic system labelIds. Will be removed")
        val exclusiveList = listOf(Inbox, Archive, Spam, Trash, Drafts, Sent)

        @Deprecated("Replaced by dynamic system labelIds. Will be removed")
        val unmodifiableByUserList = listOf(AllMail, AlmostAllMail, AllDrafts, AllSent, AllScheduled, Outbox, Snoozed)

        private fun stringOf(value: SystemLabelId): String = value.labelId.id
        fun enumOf(value: String?): SystemLabelId = map[value] ?: Inbox

        @MissingRustApi
        // Needs fixing once rust provides the list of static labelIds -> system locations
        fun fromRustSystemLabelEnum() = Inbox
    }
}

@Deprecated("Replaced by dynamic system labelIds. Will be removed")
fun List<LabelId>.filterUnmodifiableLabels(): List<LabelId> = this - unmodifiableByUserList.map { it.labelId }.toSet()

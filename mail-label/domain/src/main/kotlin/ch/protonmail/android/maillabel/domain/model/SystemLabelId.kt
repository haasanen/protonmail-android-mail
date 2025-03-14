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

enum class SystemLabelId(val labelId: LabelId) {

    /** Displayed. */
    Inbox(LabelId("0")),

    /**
     * Dynamically displayed.
     * All the draft mails have this label.
     * This is necessary because a Draft could have been moved into another folder than `Drafts`.
     */
    AllDrafts(LabelId("1")),

    /**
     * Dynamically displayed.
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

    /* Dynamically displayed. */
    Outbox(LabelId("9")),

    /* Displayed. */
    Starred(LabelId("10")),

    /* Dynamically displayed. */
    AllScheduled(LabelId("12")),

    /* Dynamically displayed. */
    AlmostAllMail(LabelId("15")),

    /* Dynamically displayed. */
    Snoozed(LabelId("16"));

    companion object {

        private val map = entries.associateBy { stringOf(it) }

        private fun stringOf(value: SystemLabelId): String = value.labelId.id
        fun enumOf(value: String?): SystemLabelId = map[value] ?: Inbox
    }
}

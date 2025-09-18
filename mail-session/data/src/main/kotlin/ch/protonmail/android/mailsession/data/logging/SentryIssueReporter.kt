/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsession.data.logging

import timber.log.Timber
import uniffi.proton_issue_reporter_service_uniffi.IssueLevel
import uniffi.proton_issue_reporter_service_uniffi.IssueReporter
import uniffi.proton_issue_reporter_service_uniffi.UserIssueReporter
import javax.inject.Inject

class SentryIssueReporter @Inject constructor() : IssueReporter {

    override fun report(
        level: IssueLevel,
        message: String,
        keys: Map<String, String>
    ) {
        timberLog(level, message, keys)

    }

    override fun newUserReporter(userId: String): UserIssueReporter {
        return object : UserIssueReporter {
            override fun report(
                level: IssueLevel,
                message: String,
                keys: Map<String, String>
            ) {
                timberLog(level, message, keys, userId)
            }

        }
    }

    private fun timberLog(
        level: IssueLevel,
        message: String,
        keys: Map<String, String>,
        userId: String? = null
    ) {
        val payload = when {
            userId != null -> keys.toMutableMap().put("userId", userId)
            else -> keys
        }
        when (level) {
            IssueLevel.CRITICAL,
            IssueLevel.ERROR -> Timber.e(message, payload)

            IssueLevel.WARNING -> Timber.w(message, payload)
        }
    }


}

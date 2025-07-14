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

package me.proton.android.core.auth.presentation.passvalidator

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.android.core.auth.presentation.R
import me.proton.core.passvalidator.domain.usecase.ValidatePassword
import me.proton.core.passvalidator.presentation.report.PasswordPolicyReportAction
import me.proton.core.passvalidator.presentation.report.PasswordPolicyReportMessage
import me.proton.core.passvalidator.presentation.report.PasswordPolicyReportState
import me.proton.core.passvalidator.presentation.report.PasswordPolicyReportViewModel
import javax.inject.Inject

@HiltViewModel
class PasswordValidatorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    validatePassword: ValidatePassword
) : PasswordPolicyReportViewModel(validatePassword) {

    override fun onAction(action: PasswordPolicyReportAction): Flow<PasswordPolicyReportState> =
        super.onAction(action).map {
            when (it) {
                is PasswordPolicyReportState.Idle -> it.copy(messages = it.messages.replaceMessages(context))
                else -> it
            }
        }
}

private fun List<PasswordPolicyReportMessage>.replaceMessages(context: Context): List<PasswordPolicyReportMessage> =
    map { msg ->
        when (msg) {
            is PasswordPolicyReportMessage.Error -> msg.copy(message = msg.message.replaceErrorMessage(context))
            is PasswordPolicyReportMessage.Hint -> msg.copy(message = msg.message.replaceErrorMessage(context))
            is PasswordPolicyReportMessage.Requirement -> msg.copy(
                message = msg.message.replaceRequirementMessage(
                    context
                )
            )
        }
    }

private val MinLengthRegex = Regex("^MIN_LENGTH_(\\d+)$")

private fun String.replaceErrorMessage(context: Context): String = MinLengthRegex.matchEntire(this)?.let {
    val minLength = it.groupValues[1].toInt()
    context.resources.getQuantityString(R.plurals.password_validator_min_length_error, minLength, minLength)
} ?: this

private fun String.replaceRequirementMessage(context: Context): String = MinLengthRegex.matchEntire(this)?.let {
    val minLength = it.groupValues[1].toInt()
    context.resources.getQuantityString(R.plurals.password_validator_min_length_requirement, minLength, minLength)
} ?: this

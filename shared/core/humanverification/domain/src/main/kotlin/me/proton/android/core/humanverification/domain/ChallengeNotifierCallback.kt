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

package me.proton.android.core.humanverification.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import me.proton.android.core.humanverification.domain.entity.HumanVerificationPayload
import me.proton.android.core.humanverification.domain.entity.HumanVerificationState
import uniffi.proton_mail_uniffi.ChallengeNotifier
import uniffi.proton_mail_uniffi.ChallengePayload
import uniffi.proton_mail_uniffi.ChallengeResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeNotifierCallback @Inject constructor() : ChallengeNotifier {

    private val mutableHumanVerificationSharedFlow = MutableSharedFlow<HumanVerificationState>(extraBufferCapacity = 1)

    override suspend fun onChallenge(payload: ChallengePayload): ChallengeResponse {
        val baseUrl = payload.base()
        val pathUrl = payload.path()
        val queryUrl = payload.query()
        val verificationToken = payload.token()
        val verificationMethods = payload.methods()

        mutableHumanVerificationSharedFlow.tryEmit(
            HumanVerificationState.HumanVerificationNeeded(
                HumanVerificationPayload(
                    baseUrl = baseUrl,
                    path = pathUrl,
                    verificationToken = verificationToken,
                    verificationMethods = verificationMethods,
                    query = queryUrl
                        .filter { it.`val` != null }
                        .map {
                            Pair(it.key, it.`val`!!)
                        }
                )
            )
        )

        val state = mutableHumanVerificationSharedFlow
            .distinctUntilChanged()
            .filter {
                when (it) {
                    is HumanVerificationState.HumanVerificationCancel,
                    is HumanVerificationState.HumanVerificationFailed,
                    is HumanVerificationState.HumanVerificationSuccess -> true

                    else -> false
                }
            }
            .first()
        return when (state) {
            is HumanVerificationState.HumanVerificationCancel -> ChallengeResponse.Cancelled
            is HumanVerificationState.HumanVerificationFailed -> ChallengeResponse.Failure
            is HumanVerificationState.HumanVerificationSuccess -> ChallengeResponse.Success(
                token = state.token,
                ttype = state.tokenType
            )

            else -> ChallengeResponse.Failure
        }
    }

    fun observeHumanVerification(): SharedFlow<HumanVerificationState> =
        mutableHumanVerificationSharedFlow.asSharedFlow()

    fun onHumanVerificationSuccess(tokenType: String, tokenCode: String) {
        mutableHumanVerificationSharedFlow.tryEmit(
            HumanVerificationState.HumanVerificationSuccess(
                token = tokenCode,
                tokenType = tokenType
            )
        )
    }

    fun onHumanVerificationCancel() {
        mutableHumanVerificationSharedFlow.tryEmit(HumanVerificationState.HumanVerificationCancel)
    }

    fun onHumanVerificationFailed() {
        mutableHumanVerificationSharedFlow.tryEmit(HumanVerificationState.HumanVerificationFailed)
    }
}

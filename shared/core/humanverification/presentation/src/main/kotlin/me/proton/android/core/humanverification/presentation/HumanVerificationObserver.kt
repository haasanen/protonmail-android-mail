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

package me.proton.android.core.humanverification.presentation

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.android.core.humanverification.domain.ChallengeNotifierCallback
import me.proton.android.core.humanverification.domain.entity.HumanVerificationState
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.presentation.app.AppLifecycleObserver
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HumanVerificationObserver @Inject constructor(
    private val activityProvider: ActivityProvider,
    appLifecycleObserver: AppLifecycleObserver,
    private val challengeNotifierCallback: ChallengeNotifierCallback
) {

    private val lifecycle = appLifecycleObserver.lifecycle

    fun observe() {
        challengeNotifierCallback.observeHumanVerification()
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .filterIsInstance<HumanVerificationState.HumanVerificationNeeded>()
            .onEach {
                activityProvider.lastResumed?.let { activity ->
                    startHumanVerification(
                        activity,
                        with(it.payload) {
                            HumanVerificationInput(
                                baseUrl = baseUrl,
                                path = path,
                                query = query,
                                verificationToken = verificationToken,
                                verificationMethods = verificationMethods
                            )
                        }
                    )
                    CoreLogger.v(LogTag.CHALLENGE_HUMAN_VERIFICATION, "Human Verification called")
                }
            }
            .launchIn(lifecycle.coroutineScope)
    }

    private fun startHumanVerification(activity: Activity, input: HumanVerificationInput) {
        val intent = Intent(activity, HumanVerificationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(HumanVerificationActivity.ARG_INPUT, input)
        }
        activity.startActivityForResult(intent, 0)
    }
}

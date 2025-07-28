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

package ch.protonmail.android.mailcomposer.presentation.viewmodel

import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcomposer.domain.model.MessagePassword
import ch.protonmail.android.mailcomposer.domain.usecase.DeleteMessagePassword
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessagePassword
import ch.protonmail.android.mailcomposer.domain.usecase.SaveMessagePassword
import ch.protonmail.android.mailcomposer.presentation.model.MessagePasswordOperation
import ch.protonmail.android.mailcomposer.presentation.model.SetMessagePasswordState
import ch.protonmail.android.mailcomposer.presentation.reducer.SetMessagePasswordReducer
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.util.kotlin.EMPTY_STRING
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class SetMessagePasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val deleteMessagePassword = mockk<DeleteMessagePassword>()
    private val observeMessagePassword = mockk<ObserveMessagePassword>()
    private val saveMessagePassword = mockk<SaveMessagePassword>()

    private val setMessagePasswordViewModel by lazy {
        SetMessagePasswordViewModel(
            deleteMessagePassword,
            observeMessagePassword,
            SetMessagePasswordReducer(),
            saveMessagePassword
        )
    }

    @Test
    fun `should initialize screen with correct values when message password does not exist`() = runTest {
        // Given
        coEvery { observeMessagePassword() } returns flowOf(null)

        // When
        setMessagePasswordViewModel.state.test {
            // Then
            val expected = SetMessagePasswordState.Data(
                initialMessagePasswordValue = EMPTY_STRING,
                initialMessagePasswordHintValue = EMPTY_STRING,
                hasMessagePasswordError = false,
                hasRepeatedMessagePasswordError = false,
                isInEditMode = false,
                exitScreen = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should initialize screen with correct values when message password exists`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val messageId = MessageIdSample.NewDraftWithSubjectAndBody
        val password = "password"
        val passwordHint = "password hint"
        val messagePassword = MessagePassword(userId, messageId, password, passwordHint)
        coEvery { observeMessagePassword() } returns flowOf(messagePassword)

        // When
        setMessagePasswordViewModel.state.test {
            // Then
            val expected = SetMessagePasswordState.Data(
                initialMessagePasswordValue = password,
                initialMessagePasswordHintValue = passwordHint,
                hasMessagePasswordError = false,
                hasRepeatedMessagePasswordError = false,
                isInEditMode = true,
                exitScreen = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should validate password when validate password action is submitted and password length is 3`() = runTest {
        // Given
        val password = "123"
        coEvery { observeMessagePassword() } returns flowOf(null)

        // When
        setMessagePasswordViewModel.submit(MessagePasswordOperation.Action.ValidatePassword(password))

        // Then
        setMessagePasswordViewModel.state.test {
            val expected = SetMessagePasswordState.Data(
                initialMessagePasswordValue = EMPTY_STRING,
                initialMessagePasswordHintValue = EMPTY_STRING,
                hasMessagePasswordError = true,
                hasRepeatedMessagePasswordError = false,
                isInEditMode = false,
                exitScreen = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should validate password when validate password action is submitted and password length is 22`() = runTest {
        // Given
        val password = "1234567890123456789012"
        coEvery { observeMessagePassword() } returns flowOf(null)

        // When
        setMessagePasswordViewModel.submit(MessagePasswordOperation.Action.ValidatePassword(password))

        // Then
        setMessagePasswordViewModel.state.test {
            val expected = SetMessagePasswordState.Data(
                initialMessagePasswordValue = EMPTY_STRING,
                initialMessagePasswordHintValue = EMPTY_STRING,
                hasMessagePasswordError = true,
                hasRepeatedMessagePasswordError = false,
                isInEditMode = false,
                exitScreen = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should validate password when validate password action is submitted and password length is 12`() = runTest {
        // Given
        val password = "123456789012"
        coEvery { observeMessagePassword() } returns flowOf(null)

        // When
        setMessagePasswordViewModel.submit(MessagePasswordOperation.Action.ValidatePassword(password))

        // Then
        setMessagePasswordViewModel.state.test {
            val expected = SetMessagePasswordState.Data(
                initialMessagePasswordValue = EMPTY_STRING,
                initialMessagePasswordHintValue = EMPTY_STRING,
                hasMessagePasswordError = false,
                hasRepeatedMessagePasswordError = false,
                isInEditMode = false,
                exitScreen = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should validate repeated password when action is submitted and passwords are matching`() = runTest {
        // Given
        val password = "123456789012"
        val repeatedPassword = "123456789012"
        coEvery { observeMessagePassword() } returns flowOf(null)

        // When
        setMessagePasswordViewModel.submit(
            MessagePasswordOperation.Action.ValidateRepeatedPassword(
                password, repeatedPassword
            )
        )

        // Then
        setMessagePasswordViewModel.state.test {
            val expected = SetMessagePasswordState.Data(
                initialMessagePasswordValue = EMPTY_STRING,
                initialMessagePasswordHintValue = EMPTY_STRING,
                hasMessagePasswordError = false,
                hasRepeatedMessagePasswordError = false,
                isInEditMode = false,
                exitScreen = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should validate repeated password when action is submitted and passwords are not matching`() = runTest {
        // Given
        val password = "123456789012"
        val repeatedPassword = "123456789"
        coEvery { observeMessagePassword() } returns flowOf(null)

        // When
        setMessagePasswordViewModel.submit(
            MessagePasswordOperation.Action.ValidateRepeatedPassword(
                password, repeatedPassword
            )
        )

        // Then
        setMessagePasswordViewModel.state.test {
            val expected = SetMessagePasswordState.Data(
                initialMessagePasswordValue = EMPTY_STRING,
                initialMessagePasswordHintValue = EMPTY_STRING,
                hasMessagePasswordError = false,
                hasRepeatedMessagePasswordError = true,
                isInEditMode = false,
                exitScreen = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should save message password and close the screen when apply password action is submitted`() = runTest {
        // Given
        val password = "password"
        val passwordHint = "password hint"
        coEvery { observeMessagePassword() } returns flowOf(null)
        coEvery { saveMessagePassword(password, passwordHint) } returns Unit.right()

        // When
        setMessagePasswordViewModel.submit(MessagePasswordOperation.Action.ApplyPassword(password, passwordHint))

        // Then
        setMessagePasswordViewModel.state.test {
            val item = awaitItem() as SetMessagePasswordState.Data
            assertEquals(Effect.of(Unit), item.exitScreen)
            coVerify { saveMessagePassword(password, passwordHint) }
        }
    }

    @Test
    fun `should update message password and close the screen when update password action is submitted`() = runTest {
        // Given
        val password = "password"
        val passwordHint = "password hint"
        coEvery { observeMessagePassword() } returns flowOf(null)
        coEvery { saveMessagePassword(password, passwordHint) } returns Unit.right()

        // When
        setMessagePasswordViewModel.submit(MessagePasswordOperation.Action.UpdatePassword(password, passwordHint))

        // Then
        setMessagePasswordViewModel.state.test {
            val item = awaitItem() as SetMessagePasswordState.Data
            assertEquals(Effect.of(Unit), item.exitScreen)
            coVerify { saveMessagePassword(password, passwordHint) }
        }
    }

    @Test
    fun `should delete message password and close the screen when delete password action is submitted`() = runTest {
        // Given
        coEvery { deleteMessagePassword() } returns Unit.right()
        coEvery { observeMessagePassword() } returns flowOf(null)

        // When
        setMessagePasswordViewModel.submit(MessagePasswordOperation.Action.RemovePassword)

        // Then
        setMessagePasswordViewModel.state.test {
            val item = awaitItem() as SetMessagePasswordState.Data
            assertEquals(Effect.of(Unit), item.exitScreen)
            coVerify { deleteMessagePassword() }
        }
    }
}

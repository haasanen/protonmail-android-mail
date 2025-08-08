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

package ch.protonmail.android.mailcommon.data.repository

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.UndoableOperation
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class UndoRepositoryImplTest {

    private lateinit var undoRepositoryImpl: UndoRepositoryImpl

    @BeforeTest
    fun setup() {
        undoRepositoryImpl = UndoRepositoryImpl()
    }

    @Test
    fun `initial operation should be null`() {
        // Then
        assertNull(undoRepositoryImpl.getLastOperation())
    }

    @Test
    fun `setting last operation saves it in memory`() {
        // Given
        val undoableOperation = UndoableOperation { println().right() }

        // When
        undoRepositoryImpl.setLastOperation(undoableOperation)
        val lastOperation = undoRepositoryImpl.getLastOperation()

        // Then
        assertEquals(undoableOperation, lastOperation)
    }

    @Test
    fun `clearing operations deletes it`() {
        // Given
        val undoableOperation = UndoableOperation { println().right() }
        undoRepositoryImpl.setLastOperation(undoableOperation)

        // When
        val lastOperation = undoRepositoryImpl.getLastOperation()
        assertEquals(undoableOperation, lastOperation)

        // Then
        undoRepositoryImpl.clearUndo()
        assertNull(undoRepositoryImpl.getLastOperation())
    }
}

package ch.protonmail.android.mailpagination.data.extension

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailpagination.data.model.AppendEvent
import ch.protonmail.android.mailpagination.data.model.PagingEvent
import ch.protonmail.android.mailpagination.data.model.RefreshEvent
import ch.protonmail.android.mailpagination.domain.model.PaginationError
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PagingEventExtTest {

    @Test
    fun `filter append events filters out events that are not Append or Error`() = runTest {
        // Given
        val inputFlow = flowOf(
            PagingEvent.Append(listOf("item1", "item2")),
            PagingEvent.Error(PaginationError.Other(DataError.Local.NoUserSession)),
            PagingEvent.Refresh(listOf("refresh item")),
            PagingEvent.Invalidate
        )
        val expected = listOf(
            AppendEvent.Append(listOf("item1", "item2")),
            AppendEvent.Error(PaginationError.Other(DataError.Local.NoUserSession))
        )

        // When
        val result = inputFlow.filterAppendEvents().toList()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `filter refresh events filters out events that are not Refresh or Error`() = runTest {
        // Given
        val inputFlow = flowOf(
            PagingEvent.Append(listOf("item1", "item2")),
            PagingEvent.Error(PaginationError.PaginationDataNotSynced),
            PagingEvent.Refresh(listOf("refresh item")),
            PagingEvent.Invalidate
        )
        val expected = listOf(
            RefreshEvent.Error(PaginationError.PaginationDataNotSynced),
            RefreshEvent.Refresh(listOf("refresh item"))
        )

        // When
        val result = inputFlow.filterRefreshEvents().toList()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `refresh event to either returns right when event is refresh`() = runTest {
        // Given
        val inputFlow = flowOf(RefreshEvent.Refresh(listOf("item1", "item2")))

        // When
        val result = inputFlow.refreshEventToEither()

        // Then
        assertEquals(listOf("item1", "item2").right(), result)
    }

    @Test
    fun `refresh event to either returns left when event is error`() = runTest {
        // Given
        val inputFlow = flowOf(RefreshEvent.Error<String>(PaginationError.PaginationDataNotSynced))

        // When
        val result = inputFlow.refreshEventToEither()

        // Then
        assertEquals(PaginationError.PaginationDataNotSynced.left(), result)
    }

    @Test
    fun `append event to either returns right when event is append`() = runTest {
        // Given
        val inputFlow = flowOf(AppendEvent.Append(listOf("item1", "item2")))

        // When
        val result = inputFlow.appendEventToEither()

        // Then
        assertEquals(listOf("item1", "item2").right(), result)
    }

    @Test
    fun `append event to either returns left when event is error`() = runTest {
        // Given
        val inputFlow = flowOf(AppendEvent.Error<String>(PaginationError.PaginationDataNotSynced))

        // When
        val result = inputFlow.appendEventToEither()

        // Then
        assertEquals(PaginationError.PaginationDataNotSynced.left(), result)
    }

}

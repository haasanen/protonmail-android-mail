package ch.protonmail.android.mailmailbox.domain.usecase

import android.graphics.Color
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailconversation.domain.usecase.GetConversationMoveToLocations
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MoveToItemId
import ch.protonmail.android.mailmessage.domain.usecase.GetMessageMoveToLocations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import me.proton.core.mailsettings.domain.entity.ViewMode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetMoveToLocationsTest {

    private val getConversationMoveToLocations = mockk<GetConversationMoveToLocations>()
    private val getMessageMoveToLocations = mockk<GetMessageMoveToLocations>()

    private val getMoveToLocations = GetMoveToLocations(
        getMessageMoveToLocations,
        getConversationMoveToLocations
    )

    @Before
    fun setUp() {
        mockkStatic(Color::class)
        every { Color.parseColor(any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Color::class)
    }

    @Test
    fun `gets available move to actions for messages when view mode for selected items is message`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MoveToItemId("1"))
        val messageIds = items.map { MessageId(it.value) }
        val viewMode = ViewMode.NoConversationGrouping
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        coEvery { getMessageMoveToLocations(userId, labelId, messageIds) } returns expected.right()

        // When
        val actual = getMoveToLocations(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `gets available move to actions for conversation when view mode for selected items is convo`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MoveToItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val expected = listOf(
            MailLabel.System(MailLabelId.System(LabelId("2")), SystemLabelId.Archive, 0),
            MailLabel.System(MailLabelId.System(LabelId("3")), SystemLabelId.Trash, 0)
        )
        coEvery {
            getConversationMoveToLocations(userId, labelId, convoIds)
        } returns expected.right()


        // When
        val actual = getMoveToLocations(userId, labelId, items, viewMode)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when getting system actions fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val labelId = LabelIdSample.Trash
        val items = listOf(MoveToItemId("1"))
        val convoIds = items.map { ConversationId(it.value) }
        val viewMode = ViewMode.ConversationGrouping
        val error = DataError.Local.Unknown.left()
        coEvery { getConversationMoveToLocations(userId, labelId, convoIds) } returns error

        // When
        val actual = getMoveToLocations(userId, labelId, items, viewMode)

        // Then
        assertEquals(error, actual)
    }
}

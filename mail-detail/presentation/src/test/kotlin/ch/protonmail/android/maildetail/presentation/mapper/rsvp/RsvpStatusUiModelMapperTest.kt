package ch.protonmail.android.maildetail.presentation.mapper.rsvp

import ch.protonmail.android.mailmessage.domain.model.RsvpAttendance
import ch.protonmail.android.mailmessage.domain.model.RsvpProgress
import ch.protonmail.android.mailmessage.domain.model.RsvpState
import ch.protonmail.android.mailmessage.domain.model.RsvpUnanswerableReason
import ch.protonmail.android.maildetail.presentation.model.RsvpStatusUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RsvpStatusUiModelMapperTest {

    private val mapper = RsvpStatusUiModelMapper()

    @Test
    fun `when AnswerableInvite with Pending progress, returns null`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Pending, attendance = RsvpAttendance.Optional)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertNull(result)
    }

    @Test
    fun `when AnswerableInvite with Ongoing progress, returns HappeningNow`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ongoing, attendance = RsvpAttendance.Optional)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.HappeningNow, result)
    }

    @Test
    fun `when AnswerableInvite with Ended progress, returns EventEnded`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ended, attendance = RsvpAttendance.Optional)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.EventEnded, result)
    }

    @Test
    fun `when CancelledInvite is outdated, returns EventCancelledInviteOutdated`() {
        // Given
        val state = RsvpState.CancelledInvite(isOutdated = true)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.EventCancelledInviteOutdated, result)
    }

    @Test
    fun `when CancelledInvite is not outdated, returns EventCancelled`() {
        // Given
        val state = RsvpState.CancelledInvite(isOutdated = false)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.EventCancelled, result)
    }

    @Test
    fun `when CancelledReminder, returns EventCancelled`() {
        // Given
        val state = RsvpState.CancelledReminder

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.EventCancelled, result)
    }

    @Test
    fun `when Reminder with Pending progress, returns null`() {
        // Given
        val state = RsvpState.Reminder(progress = RsvpProgress.Pending)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertNull(result)
    }

    @Test
    fun `when Reminder with Ongoing progress, returns HappeningNow`() {
        // Given
        val state = RsvpState.Reminder(progress = RsvpProgress.Ongoing)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.HappeningNow, result)
    }

    @Test
    fun `when Reminder with Ended progress, returns EventEnded`() {
        // Given
        val state = RsvpState.Reminder(progress = RsvpProgress.Ended)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.EventEnded, result)
    }

    @Test
    fun `when UnanswerableInvite with InviteIsOutdated reason, returns InviteOutdated`() {
        // Given
        val state = RsvpState.UnanswerableInvite(reason = RsvpUnanswerableReason.InviteIsOutdated)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.InviteOutdated, result)
    }

    @Test
    fun `when UnanswerableInvite with AddressIsIncorrect reason, returns AddressIsIncorrect`() {
        // Given
        val state = RsvpState.UnanswerableInvite(reason = RsvpUnanswerableReason.AddressIsIncorrect)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.AddressIsIncorrect, result)
    }

    @Test
    fun `when UnanswerableInvite with UserIsOrganizer reason, returns UserIsOrganizer`() {
        // Given
        val state = RsvpState.UnanswerableInvite(reason = RsvpUnanswerableReason.UserIsOrganizer)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.UserIsOrganizer, result)
    }

    @Test
    fun `when UnanswerableInvite with EventDoesNotExist reason, returns EventDoesNotExist`() {
        // Given
        val state = RsvpState.UnanswerableInvite(reason = RsvpUnanswerableReason.EventDoesNotExist)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.EventDoesNotExist, result)
    }

    @Test
    fun `when UnanswerableInvite with NetworkFailure reason, returns NetworkFailure`() {
        // Given
        val state = RsvpState.UnanswerableInvite(reason = RsvpUnanswerableReason.NetworkFailure)

        // When
        val result = mapper.toUiModel(state)

        // Then
        assertEquals(RsvpStatusUiModel.NetworkFailure, result)
    }
}

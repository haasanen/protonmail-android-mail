package ch.protonmail.android.maildetail.presentation.mapper.rsvp

import ch.protonmail.android.maildetail.domain.model.RsvpAttendance
import ch.protonmail.android.maildetail.domain.model.RsvpProgress
import ch.protonmail.android.maildetail.domain.model.RsvpState
import ch.protonmail.android.maildetail.domain.model.RsvpUnanswerableReason
import ch.protonmail.android.maildetail.presentation.model.RsvpAnswer
import ch.protonmail.android.maildetail.presentation.model.RsvpButtonsUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class RsvpButtonsUiModelMapperTest {

    private val mapper = RsvpButtonsUiModelMapper()

    @Test
    fun `when AnswerableInvite with Pending progress and Yes answer, returns Shown with Yes`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Pending, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Yes

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Shown(RsvpAnswer.Yes), result)
    }

    @Test
    fun `when AnswerableInvite with Pending progress and No answer, returns Shown with No`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Pending, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.No

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Shown(RsvpAnswer.No), result)
    }

    @Test
    fun `when AnswerableInvite with Pending progress and Maybe answer, returns Shown with Maybe`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Pending, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Maybe

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Shown(RsvpAnswer.Maybe), result)
    }

    @Test
    fun `when AnswerableInvite with Pending progress and Unanswered, returns Shown with Unanswered`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Pending, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Unanswered

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Shown(RsvpAnswer.Unanswered), result)
    }

    @Test
    fun `when AnswerableInvite with Ongoing progress and Yes answer, returns Shown with Yes`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ongoing, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Yes

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Shown(RsvpAnswer.Yes), result)
    }

    @Test
    fun `when AnswerableInvite with Ongoing progress and No answer, returns Shown with No`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ongoing, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.No

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Shown(RsvpAnswer.No), result)
    }

    @Test
    fun `when AnswerableInvite with Ongoing progress and Maybe answer, returns Shown with Maybe`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ongoing, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Maybe

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Shown(RsvpAnswer.Maybe), result)
    }

    @Test
    fun `when AnswerableInvite with Ongoing progress and Unanswered, returns Shown with Unanswered`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ongoing, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Unanswered

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Shown(RsvpAnswer.Unanswered), result)
    }

    @Test
    fun `when AnswerableInvite with Ended progress and Yes answer, returns Hidden`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ended, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Yes

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Hidden, result)
    }

    @Test
    fun `when AnswerableInvite with Ended progress and No answer, returns Hidden`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ended, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.No

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Hidden, result)
    }

    @Test
    fun `when AnswerableInvite with Ended progress and Maybe answer, returns Hidden`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ended, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Maybe

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Hidden, result)
    }

    @Test
    fun `when AnswerableInvite with Ended progress and Unanswered, returns Hidden`() {
        // Given
        val state = RsvpState.AnswerableInvite(progress = RsvpProgress.Ended, attendance = RsvpAttendance.Optional)
        val attendeeAnswer = RsvpAnswer.Unanswered

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Hidden, result)
    }

    @Test
    fun `when CancelledInvite, returns Hidden`() {
        // Given
        val state = RsvpState.CancelledInvite(isOutdated = false)
        val attendeeAnswer = RsvpAnswer.Yes

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Hidden, result)
    }

    @Test
    fun `when CancelledReminder, returns Hidden`() {
        // Given
        val state = RsvpState.CancelledReminder
        val attendeeAnswer = RsvpAnswer.Maybe

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Hidden, result)
    }

    @Test
    fun `when Reminder, returns Hidden`() {
        // Given
        val state = RsvpState.Reminder(progress = RsvpProgress.Pending)
        val attendeeAnswer = RsvpAnswer.Yes

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Hidden, result)
    }

    @Test
    fun `when UnanswerableInvite, returns Hidden`() {
        // Given
        val state = RsvpState.UnanswerableInvite(reason = RsvpUnanswerableReason.InviteIsOutdated)
        val attendeeAnswer = RsvpAnswer.Unanswered

        // When
        val result = mapper.toUiModel(state, attendeeAnswer)

        // Then
        assertEquals(RsvpButtonsUiModel.Hidden, result)
    }
}

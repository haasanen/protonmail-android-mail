package ch.protonmail.android.mailmailbox.data.repository

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalAutoDeleteBanner
import ch.protonmail.android.mailcommon.data.mapper.LocalAutoDeleteState
import ch.protonmail.android.mailcommon.data.mapper.LocalSpamOrTrash
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmailbox.data.mapper.toAutoDeleteBanner
import ch.protonmail.android.mailmailbox.data.usecase.RustGetAutoDeleteBanner
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MailboxBannersRepositoryImplTest {

    private val rustGetAutoDeleteBanner = mockk<RustGetAutoDeleteBanner>()
    private val userSessionRepository = mockk<UserSessionRepository>()

    private val mailboxBannersRepository = MailboxBannersRepositoryImpl(
        rustGetAutoDeleteBanner = rustGetAutoDeleteBanner,
        userSessionRepository = userSessionRepository
    )

    @Test
    fun `should return error when session is null`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LabelIdSample.Trash
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val actual = mailboxBannersRepository.getAutoDeleteBanner(userId, labelId)

        // Then
        assertEquals(DataError.Local.NoUserSession.left(), actual)
    }

    @Test
    fun `should return error when getting the auto delete banner fails`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LabelIdSample.Trash
        val userSessionMock = mockk<MailUserSessionWrapper>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock
        coEvery {
            rustGetAutoDeleteBanner(userSessionMock, labelId.toLocalLabelId())
        } returns DataError.Local.Unknown.left()

        // When
        val actual = mailboxBannersRepository.getAutoDeleteBanner(userId, labelId)

        // Then
        assertEquals(DataError.Local.Unknown.left(), actual)
    }

    @Test
    fun `should return auto delete banner when getting the auto delete banner succeeds`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LabelIdSample.Trash
        val userSessionMock = mockk<MailUserSessionWrapper>()
        val localAutoDeleteBanner = LocalAutoDeleteBanner(
            LocalAutoDeleteState.AUTO_DELETE_ENABLED,
            LocalSpamOrTrash.TRASH
        )
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock
        coEvery {
            rustGetAutoDeleteBanner(userSessionMock, labelId.toLocalLabelId())
        } returns localAutoDeleteBanner.right()

        // When
        val actual = mailboxBannersRepository.getAutoDeleteBanner(userId, labelId)

        // Then
        assertEquals(localAutoDeleteBanner.toAutoDeleteBanner().right(), actual)
    }
}

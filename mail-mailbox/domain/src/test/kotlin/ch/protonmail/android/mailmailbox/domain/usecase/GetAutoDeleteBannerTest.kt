package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.domain.sample.LabelIdSample
import ch.protonmail.android.mailmailbox.domain.model.AutoDeleteBanner
import ch.protonmail.android.mailmailbox.domain.model.AutoDeleteState
import ch.protonmail.android.mailmailbox.domain.model.SpamOrTrash
import ch.protonmail.android.mailmailbox.domain.repository.MailboxBannersRepository
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAutoDeleteBannerTest {

    private val mailboxBannersRepository = mockk<MailboxBannersRepository>()

    private val getAutoDeleteBanner = GetAutoDeleteBanner(mailboxBannersRepository)

    @Test
    fun `should return error when getting the auto delete banner fails`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LabelIdSample.Trash
        coEvery {
            mailboxBannersRepository.getAutoDeleteBanner(
                userId,
                labelId
            )
        } returns DataError.Local.CryptoError.left()

        // When
        val actual = getAutoDeleteBanner(userId, labelId)

        // Then
        assertEquals(DataError.Local.CryptoError.left(), actual)
    }

    @Test
    fun `should return the auto delete banner when getting the auto delete banner succeeds`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val labelId = LabelIdSample.Trash
        val autoDeleteBanner = AutoDeleteBanner(AutoDeleteState.AutoDeleteEnabled, SpamOrTrash.Trash)
        coEvery {
            mailboxBannersRepository.getAutoDeleteBanner(userId, labelId)
        } returns autoDeleteBanner.right()

        // When
        val actual = getAutoDeleteBanner(userId, labelId)

        // Then
        assertEquals(autoDeleteBanner.right(), actual)
    }
}

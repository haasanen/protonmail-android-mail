package ch.protonmail.android.mailcomposer.presentation.usecase

import android.content.Context
import ch.protonmail.android.mailcomposer.domain.model.ComposerValues.EDITOR_ID
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.ui.JAVASCRIPT_CALLBACK_INTERFACE_NAME
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals

internal class GetCustomJsTest {

    private val mockContext = mockk<Context>()
    private val mockFeatureFlag = mockk<FeatureFlag<Boolean>> {
        coEvery { this@mockk.get() } returns false
    }

    private val getCustomJs = GetCustomJs(mockContext, mockFeatureFlag)

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `replace editor id constant with actual value`() = runTest {
        // Given
        every {
            mockContext.resources.openRawResource(R.raw.rich_text_editor)
        } returns "js \$EDITOR_ID more js \$EDITOR_ID".byteInputStream()
        val expected = "js $EDITOR_ID more js $EDITOR_ID"

        // When
        val actual = getCustomJs()

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `replace javascript callback constant with actual value`() = runTest {
        // Given
        every {
            mockContext.resources.openRawResource(R.raw.rich_text_editor)
        } returns "js \$JAVASCRIPT_CALLBACK_INTERFACE_NAME more js".byteInputStream()
        val expected = "js $JAVASCRIPT_CALLBACK_INTERFACE_NAME more js"

        // When
        val actual = getCustomJs()

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `loads the correct resource based on the FF (true)`() = runTest {
        // Given
        coEvery { mockFeatureFlag.get() } returns true
        every {
            mockContext.resources.openRawResource(R.raw.rich_text_editor_autocollapse)
        } returns "".byteInputStream()

        // When
        getCustomJs()

        // Then
        verify(exactly = 1) { mockContext.resources.openRawResource(R.raw.rich_text_editor_autocollapse) }
        confirmVerified(mockContext.resources)
    }

    @Test
    fun `loads the correct resource based on the FF (false)`() = runTest {
        // Given
        coEvery { mockFeatureFlag.get() } returns false
        every {
            mockContext.resources.openRawResource(R.raw.rich_text_editor)
        } returns "".byteInputStream()

        // When
        getCustomJs()

        // Then
        verify(exactly = 1) { mockContext.resources.openRawResource(R.raw.rich_text_editor) }
        confirmVerified(mockContext.resources)
    }
}

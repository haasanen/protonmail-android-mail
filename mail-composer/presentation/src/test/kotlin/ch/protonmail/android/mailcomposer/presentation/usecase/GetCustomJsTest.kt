package ch.protonmail.android.mailcomposer.presentation.usecase

import android.content.Context
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.ui.JAVASCRIPT_CALLBACK_INTERFACE_NAME
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class GetCustomJsTest {

    private val mockContext = mockk<Context>()

    private val getCustomJs = GetCustomJs(mockContext)

    @Test
    fun `replace editor id constant with actual value`() {
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
    fun `replace javascript callback constant with actual value`() {
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
}

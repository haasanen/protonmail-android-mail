package ch.protonmail.android.mailmessage.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class InjectViewPortMetaTagIntoMessageBodyTest {

    private val injectViewPortMetaTagIntoMessageBody = InjectViewPortMetaTagIntoMessageBody()

    @Test
    fun `injects meta tag into existing head tag`() {
        val input = """
            <html>
            <head>
                <title>Email</title>
            </head>
            <body>Hello world</body>
            </html>
        """.trimIndent()

        val expected = """
            <html>
            <head><meta name="viewport" content="width=device-width, user-scalable=yes">
                <title>Email</title>
            </head>
            <body>Hello world</body>
            </html>
        """.trimIndent()

        val actual = injectViewPortMetaTagIntoMessageBody(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `injects head tag if missing but html tag exists`() {
        val input = """
            <html>
                <body>Hello world</body>
            </html>
        """.trimIndent()

        val expected = """
            <html>
            <head><meta name="viewport" content="width=device-width, user-scalable=yes"></head>
                <body>Hello world</body>
            </html>
        """.trimIndent()

        val actual = injectViewPortMetaTagIntoMessageBody(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `prepends head and meta tag if both head and html tags are missing`() {
        val input = """
            <body>Hello world</body>
        """.trimIndent()

        val expected = """
            <head><meta name="viewport" content="width=device-width, user-scalable=yes"></head>
            <body>Hello world</body>
        """.trimIndent()

        val actual = injectViewPortMetaTagIntoMessageBody(input)
        assertEquals(expected, actual)
    }
}

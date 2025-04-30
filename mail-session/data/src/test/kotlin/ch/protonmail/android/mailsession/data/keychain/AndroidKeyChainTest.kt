package ch.protonmail.android.mailsession.data.keychain

import arrow.core.right
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.MockKException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.OsKeyChainEntryKind
import kotlin.test.assertEquals

class AndroidKeyChainTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val keyChainLocalDataSource = mockk<KeyChainLocalDataSource>()
    private val keyStoreCrypto = object : KeyStoreCrypto {
        private var encryptResult: EncryptedString? = null
        private var decryptResult: String? = null

        fun encryptSucceeds(result: EncryptedString) { encryptResult = result }
        fun decryptSucceeds(result: String) { decryptResult = result }
        override fun decrypt(value: EncryptedString): String = decryptResult!!

        override fun decrypt(value: EncryptedByteArray): PlainByteArray {
            throw MockKException("Not stubbed")
        }

        override fun encrypt(value: String): EncryptedString = encryptResult!!

        override fun encrypt(value: PlainByteArray): EncryptedByteArray {
            throw MockKException("Not stubbed")
        }

        override fun isUsingKeyStore(): Boolean {
            throw MockKException("Not stubbed")
        }
    }
    private val dispatcher = mainDispatcherRule.testDispatcher
    private val coroutineScope = TestScope(dispatcher)

    private val keyChain = AndroidKeyChain(
        keyChainLocalDataSource,
        keyStoreCrypto,
        coroutineScope
    )

    @Test
    fun `encrypts data before storing to insecure storage`() {
        // Given
        val type = OsKeyChainEntryKind.ENCRYPTION_KEY
        val secret = "secret-key"
        val encryptedSecret = "can't touch this"
        keyStoreCrypto.encryptSucceeds(encryptedSecret)
        coEvery { keyChainLocalDataSource.save(type, encryptedSecret) } returns Unit.right()

        // When
        keyChain.store(type, secret)

        // Then
        coVerify { keyChainLocalDataSource.save(type, encryptedSecret) }
    }

    @Test
    fun `decrypts data when reading from insecure storage`() {
        // Given
        val type = OsKeyChainEntryKind.ENCRYPTION_KEY
        val expected = "secret-key"
        val encryptedSecret = "can't touch this"
        keyStoreCrypto.decryptSucceeds(expected)
        coEvery { keyChainLocalDataSource.get(type) } returns encryptedSecret.right()

        // When
        val actual = keyChain.load(type)

        // Then
        assertEquals(expected, actual)
    }

}

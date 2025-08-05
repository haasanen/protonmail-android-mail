package ch.protonmail.android.mailcomposer.presentation.usecase

import ch.protonmail.android.mailcomposer.domain.usecase.RegisterComposerInstance
import ch.protonmail.android.mailcomposer.domain.usecase.UnregisterComposerInstance
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class ActiveComposerRegistryTest {

    private val registerComposerRegistry = mockk<RegisterComposerInstance>()
    private val unregisterComposerRegistry = mockk<UnregisterComposerInstance>()

    private val activeComposerRegistry = ActiveComposerRegistry(
        registerComposerRegistry,
        unregisterComposerRegistry
    )

    @Test
    fun `forwards register call to register instance use case`() {
        // Given
        val hash = 12_238_838
        every { registerComposerRegistry.invoke(hash.toString()) } just Runs

        // When
        activeComposerRegistry.register(hash)

        // Then
        verify { registerComposerRegistry(hash.toString()) }
    }

    @Test
    fun `forwards unregister call to unregister instance use case`() {
        // Given
        val hash = 12_238_838
        every { unregisterComposerRegistry.invoke(hash.toString()) } just Runs

        // When
        activeComposerRegistry.unregister(hash)

        // Then
        verify { unregisterComposerRegistry(hash.toString()) }
    }
}

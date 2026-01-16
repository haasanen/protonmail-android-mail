package ch.protonmail.android.mailcomposer.presentation.usecase

import ch.protonmail.android.mailcomposer.domain.usecase.IsComposerInstanceActive
import ch.protonmail.android.mailcomposer.domain.usecase.RegisterComposerInstance
import ch.protonmail.android.mailcomposer.domain.usecase.UnregisterComposerInstance
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertTrue

class ActiveComposerRegistryTest {

    private val registerComposerRegistry = mockk<RegisterComposerInstance>()
    private val unregisterComposerRegistry = mockk<UnregisterComposerInstance>()
    private val isComposerInstanceActive = mockk<IsComposerInstanceActive>()

    private val activeComposerRegistry = ActiveComposerRegistry(
        registerComposerRegistry,
        unregisterComposerRegistry,
        isComposerInstanceActive
    )

    @Test
    fun `forwards register call to register instance use case`() {
        // Given
        val id = "test-composer-instance-id"
        every { registerComposerRegistry.invoke(id) } just Runs

        // When
        activeComposerRegistry.register(id)

        // Then
        verify { registerComposerRegistry(id) }
    }

    @Test
    fun `forwards unregister call to unregister instance use case`() {
        // Given
        val id = "test-composer-instance-id"
        every { unregisterComposerRegistry.invoke(id) } just Runs

        // When
        activeComposerRegistry.unregister(id)

        // Then
        verify { unregisterComposerRegistry(id) }
    }

    @Test
    fun `forwards isActive call to isActive instance use case`() {
        // Given
        val id = "test-composer-instance-id"
        every { isComposerInstanceActive.invoke(id) } returns true

        // When
        val result = activeComposerRegistry.isActive(id)

        // Then
        assertTrue(result)
        verify { isComposerInstanceActive(id) }
    }
}

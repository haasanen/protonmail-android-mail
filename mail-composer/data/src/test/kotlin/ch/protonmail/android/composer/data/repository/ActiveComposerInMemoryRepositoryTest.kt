package ch.protonmail.android.composer.data.repository

import org.junit.Test
import kotlin.test.assertEquals

class ActiveComposerInMemoryRepositoryTest {

    private val activeComposerRepository = ActiveComposerInMemoryRepository()

    @Test
    fun `register instance adds instance to the in memory list`() {
        // Given
        val instance = "12238838"

        // When
        activeComposerRepository.registerInstance(instance)

        // Then
        assertEquals(listOf(instance), activeComposerRepository.instances)
    }

    @Test
    fun `unregister instance removes it from the in memory list`() {
        // Given
        val instance = "12238838"

        // When
        activeComposerRepository.unregisterInstance(instance)

        // Then
        assertEquals(emptyList(), activeComposerRepository.instances)
    }

    @Test
    fun `get latest active instance returns the last added instance`() {
        // Given
        val firstInstance = "12238838"
        val secondInstance = "82388321"
        activeComposerRepository.instances.addAll(
            listOf(firstInstance, secondInstance)
        )

        // When
        val actual = activeComposerRepository.getLatestActiveInstance()

        // Then
        assertEquals(secondInstance, actual)
    }

    @Test
    fun `unregister callback called when instance is unregistered`() {
        // Given
        var actualUnregistered: String? = null
        val instance = "12238838"
        activeComposerRepository.instances.add(instance)

        // When
        activeComposerRepository.setUnregisterCallback {
            actualUnregistered = it
        }
        activeComposerRepository.unregisterInstance(instance)

        // Then
        assertEquals(instance, actualUnregistered)
    }
}

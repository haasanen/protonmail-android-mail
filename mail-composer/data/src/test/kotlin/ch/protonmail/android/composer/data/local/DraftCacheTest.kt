package ch.protonmail.android.composer.data.local

import ch.protonmail.android.composer.data.repository.ActiveComposerInMemoryRepository
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import org.junit.Test
import kotlin.test.assertEquals

class DraftCacheTest {

    private val activeComposerRepository = mockk<ActiveComposerInMemoryRepository> {
        every { this@mockk.setUnregisterCallback(any()) } just Runs
    }

    private val draftCache by lazy { DraftCache(activeComposerRepository) }

    @Test
    fun `add Draft to in memory cache associating it with the active composer instance`() {
        // Given
        val instanceHash = "active_instance"
        val draftWrapper = mockk<DraftWrapper>()
        val expected = mapOf(instanceHash to draftWrapper)
        every { activeComposerRepository.getLatestActiveInstance() } returns instanceHash

        // When
        draftCache.add(draftWrapper)

        // Then
        assertEquals(expected, draftCache.draftByComposerInstance)
    }

    @Test
    fun `get instance returns the cached Draft associated with the latest active composer instance`() {
        // Given
        val olderInstance = "older_instance"
        val activeInstance = "active_instance"
        val draftWrapper = mockk<DraftWrapper>()
        val olderDraftWrapper = mockk<DraftWrapper>()
        every { activeComposerRepository.getLatestActiveInstance() } returns activeInstance
        draftCache.draftByComposerInstance[activeInstance] = draftWrapper
        draftCache.draftByComposerInstance[olderInstance] = olderDraftWrapper

        // When
        val actual = draftCache.get()

        // Then
        assertEquals(draftWrapper, actual)
    }

    @Test
    fun `removes cached Draft when unregister callback is fired`() {
        // Given
        val activeInstance = "active_instance"
        val draftWrapper = mockk<DraftWrapper>()
        val callback = slot<(String) -> Unit>()
        every { activeComposerRepository.getLatestActiveInstance() } returns activeInstance
        every { activeComposerRepository.setUnregisterCallback(capture(callback)) } just Runs
        draftCache.draftByComposerInstance[activeInstance] = draftWrapper

        // When
        callback.captured.invoke(activeInstance)

        // Then
        assertEquals(emptyMap(), draftCache.draftByComposerInstance)
    }
}

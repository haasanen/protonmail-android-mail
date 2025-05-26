package ch.protonmail.android.mailcomposer.presentation.viewmodel

import java.io.File
import java.io.IOException
import java.time.LocalDate
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import app.cash.turbine.test
import ch.protonmail.android.mailcommon.data.file.FileHelper
import ch.protonmail.android.mailcommon.presentation.usecase.FormatLocalDate
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val FAKE_FORMATTED_DATE = "20 May 2025 10:20"
private const val FAKE_PACKAGE_NAME = "ch.protonmail.android"
private const val FAKE_CACHE_PATH = "/data/test/cache/path"

class CameraPicturePickerViewModelTest {

    private val expectedFilename = "camera-capture-$FAKE_FORMATTED_DATE.jpg"
    private val appAuthority = "$FAKE_PACKAGE_NAME.provider"

    private val testDispatcher = StandardTestDispatcher()
    private val nowDateMock = LocalDate.ofEpochDay(1_748_427_600) // 20 May 2025 10:20
    private val context = mockk<Context>(relaxed = true)
    private val formatLocalDate = mockk<FormatLocalDate>()
    private val fileFactory = mockk<FileHelper.FileFactory>()
    private val fileMock = mockk<File>()
    private val uriMock = mockk<Uri>()

    private val viewModel = CameraPicturePickerViewModel(
        context,
        testDispatcher,
        formatLocalDate,
        fileFactory
    )

    @BeforeTest
    fun setUp() {
        mockkStatic(LocalDate::class)
        coEvery { LocalDate.now() } returns nowDateMock
        coEvery { formatLocalDate(nowDateMock) } returns FAKE_FORMATTED_DATE
        coEvery { context.packageName } returns FAKE_PACKAGE_NAME
        coEvery { context.cacheDir.absolutePath } returns FAKE_CACHE_PATH

        mockkStatic(FileProvider::class)
        coEvery { FileProvider.getUriForFile(context, appAuthority, fileMock) } returns uriMock
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `emits Check Permission state when capture camera picture is requested`() = runTest {
        viewModel.state.test {
            // Given
            assertEquals(CameraPicturePickerViewModel.State.Initial, awaitItem())

            // When
            viewModel.onCapturePictureRequested()

            // Then
            assertEquals(CameraPicturePickerViewModel.State.CheckPermissions, awaitItem())
        }
    }

    @Test
    fun `emits file uri of generated temp file when camera permission granted`() = runTest(testDispatcher) {
        // Given
        val filename = "camera-capture-$FAKE_FORMATTED_DATE.jpg"
        coEvery {
            fileFactory.fileFrom(FileHelper.Folder(FAKE_CACHE_PATH), FileHelper.Filename(filename))
        } returns fileMock

        // When
        viewModel.onPermissionGranted()

        // Then
        viewModel.state.test {
            skipItems(1) // initial state
            val expected = CameraPicturePickerViewModel.State.FileInfo(uriMock)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `emits file error when generating temp file fails`() = runTest(testDispatcher) {
        // Given
        coEvery {
            fileFactory.fileFrom(FileHelper.Folder(FAKE_CACHE_PATH), FileHelper.Filename(expectedFilename))
        } throws IOException("Cache is full")

        // When
        viewModel.onPermissionGranted()

        // Then
        viewModel.state.test {
            skipItems(1) // initial state
            val expected = CameraPicturePickerViewModel.State.Error.FileError
            assertEquals(expected, awaitItem())
        }
    }
}

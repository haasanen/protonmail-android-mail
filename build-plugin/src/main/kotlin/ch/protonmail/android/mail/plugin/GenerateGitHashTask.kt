/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.mail.plugin

import java.io.ByteArrayOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class GenerateGitHashTask : DefaultTask() {

    @get:InputFile
    abstract val headRefFile: RegularFileProperty

    @get:InputDirectory
    abstract val refsDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun execute() {
        outputFile.get().asFile.parentFile?.mkdirs()

        val stdout = ByteArrayOutputStream()

        execOperations.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }

        val gitHash = stdout.toString().trim()
        outputFile.get().asFile.writeText(gitHash)
        logger.info("Current git hash: $gitHash")
    }
}

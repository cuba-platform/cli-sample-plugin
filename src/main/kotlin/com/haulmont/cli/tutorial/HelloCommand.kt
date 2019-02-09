/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cli.tutorial

import com.beust.jcommander.Parameters
import com.haulmont.cuba.cli.commands.AbstractCommand
import java.io.File

@Parameters(commandDescription = "Prints project structure")
class HelloCommand : AbstractCommand() {

    override fun preExecute() = checkProjectExistence()

    override fun run() {
        val currentDir = File(".").toPath()

        val osName = System.getProperty("os.name").toLowerCase()
        val gradleScriptName = when {
            osName.indexOf("win") >= 0 -> "gradlew.bat"
            else -> "gradlew"
        }
        val gradleScriptPath = currentDir.resolve(gradleScriptName).toFile().absolutePath

        val createIprGradle : List<String> = when {
            osName.indexOf("win") >= 0 -> arrayListOf("cmd", "/C", gradleScriptPath, "project")
            else -> arrayListOf(gradleScriptPath, "project")
        }

        val processBuilder = ProcessBuilder(createIprGradle)
        processBuilder.directory(currentDir.toFile())
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

        val process = processBuilder.start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            fail("Unable to find project, gradlew exit code $exitCode")
        }
    }
}
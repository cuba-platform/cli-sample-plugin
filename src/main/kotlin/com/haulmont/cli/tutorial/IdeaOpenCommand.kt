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
import com.google.common.io.CharStreams
import com.haulmont.cuba.cli.*
import com.haulmont.cuba.cli.commands.AbstractCommand
import org.kodein.di.generic.instance
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@Parameters(commandDescription = "Opens project in IntelliJ IDEA")
class IdeaOpenCommand : AbstractCommand() {

    private val writer: PrintWriter by kodein.instance()

    override fun preExecute() = checkProjectExistence()

    override fun run() {
        val model = context.getModel<ProjectModel>(ProjectModel.MODEL_NAME)
        val iprFileName = model.name + ".ipr"

        val projectStructure = ProjectStructure()

        val hasIpr = Files.exists(projectStructure.path.resolve(iprFileName))
        if (!hasIpr) {
            writer.println("Generating project files".green())

            val currentDir = projectStructure.path.toAbsolutePath()

            val osName = System.getProperty("os.name").toLowerCase()
            val gradleScriptName = when {
                osName.indexOf("win") >= 0 -> "gradlew.bat"
                else -> "gradlew"
            }
            val gradleScriptPath = currentDir.resolve(gradleScriptName).toFile().absolutePath

            val createIprGradle : List<String> = when {
                osName.indexOf("win") >= 0 -> arrayListOf("cmd", "/C", gradleScriptPath, "idea")
                else -> arrayListOf(gradleScriptPath, "idea")
            }

            val processBuilder = ProcessBuilder(createIprGradle)
            processBuilder.directory(currentDir.toFile())
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

            val process = processBuilder.start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                fail("Unable to generate project files, gradlew exit code $exitCode")
            }
        }

        val projectAbsolutePath = projectStructure.path.resolve(iprFileName).toAbsolutePath()
        val url = URL("http://localhost:48561/?project=$projectAbsolutePath")
        sendRequest(url)
    }

    private fun sendRequest(url: URL) {
        try {
            val connection = url.openConnection()
            connection.connect()
            connection.connectTimeout = 20000

            InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8).use { reader ->
                val response = CharStreams.toString(reader)
                val firstLine = response.trim { it <= ' ' }.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                if (!firstLine.startsWith("OK")) {
                    fail("Unable to connect to the IDE. Check if the IDE is running and CUBA Plugin is installed.")
                }
            }
        } catch (e: IOException) {
            fail("Unable to connect to the IDE. Check if the IDE is running and CUBA Plugin is installed.")
        }
    }
}
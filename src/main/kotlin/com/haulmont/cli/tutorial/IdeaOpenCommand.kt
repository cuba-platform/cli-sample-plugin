package com.haulmont.cli.tutorial

import com.beust.jcommander.Parameters
import com.google.common.io.CharStreams
import com.haulmont.cuba.cli.ProjectModel
import com.haulmont.cuba.cli.ProjectStructure
import com.haulmont.cuba.cli.WorkingDirectoryManager
import com.haulmont.cuba.cli.commands.AbstractCommand
import com.haulmont.cuba.cli.kodein
import org.kodein.di.generic.instance
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL
import java.nio.file.Files

@Parameters(commandDescription = "Opens project in IntelliJ IDEA")
class IdeaOpenCommand : AbstractCommand() {

    private val printWriter: PrintWriter by kodein.instance()

    private val workingDirectoryManager: WorkingDirectoryManager by kodein.instance()

    override fun preExecute() = checkProjectExistence()

    override fun run() {
        val model = context.getModel<ProjectModel>(ProjectModel.MODEL_NAME)
        val iprFileName = model.name + ".ipr"

        val projectStructure = ProjectStructure()

        val hasIpr = projectStructure.path.resolve(iprFileName).let { Files.exists(it) }
        if (!hasIpr) {
            val createIprGradle = "${workingDirectoryManager.absolutePath.resolve("gradlew")} idea"
            val process = Runtime.getRuntime().exec(createIprGradle, emptyArray(), workingDirectoryManager.absolutePath.toFile())
            process.waitFor()
        }

        val url = URL("""http://localhost:48561/?project=${projectStructure.path.resolve(iprFileName).toAbsolutePath()}""")
        sendRequest(url)
    }

    private fun sendRequest(url: URL) {
        try {
            val connection = url.openConnection()
            connection.connect()
            InputStreamReader(connection.getInputStream(), "UTF-8").use { reader ->
                val response = CharStreams.toString(reader)
                val firstLine = response.trim { it <= ' ' }.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                if (!firstLine.startsWith("OK")) {
                    printWriter.println("Unable to connect to the IDE. Check if the IDE is running and CUBA Plugin is installed.")
                }
            }
        } catch (e: IOException) {
            printWriter.println("Unable to connect to the IDE. Check if the IDE is running and CUBA Plugin is installed.")
        }
    }
}
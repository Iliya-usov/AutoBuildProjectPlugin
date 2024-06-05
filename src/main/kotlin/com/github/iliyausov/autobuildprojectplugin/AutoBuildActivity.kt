package com.github.iliyausov.autobuildprojectplugin

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.task.ProjectTaskManager
import com.intellij.util.application
import org.jetbrains.concurrency.await

class AutoBuildActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val kind = System.getProperty("auto.build.plugin.kind")?.let { AutoBuildKind.valueOf(it) } ?: AutoBuildKind.Build

        val result = ProjectTaskManager.getInstance(project).let {
            when (kind) {
                AutoBuildKind.Build -> it.buildAllModules()
                AutoBuildKind.Rebuild -> it.rebuildAllModules()
                AutoBuildKind.None -> return // do nothing
            }
        }.await()

        val exitCode = if (result.hasErrors()) 1 else 0
        invokeLater {
            application.exit(true, true, false, exitCode)
        }
    }

    enum class AutoBuildKind {
        Build, Rebuild, None
    }
}
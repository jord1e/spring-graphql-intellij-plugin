package nl.jrdie.idea.springql.ide.diagnostics

import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.Consumer
import nl.jrdie.idea.springql.QLBundle
import java.awt.Component
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class QLGitHubErrorReporter : ErrorReportSubmitter() {

    private companion object {
        private const val GITHUB_ISSUE_URL = "https://github.com/jord1e/spring-graphql-intellij-plugin/issues/new?"
        private const val STACKTRACE_LENGTH = 5000
        private const val SPRING_GRAPHQL_SUPPORT_PLUGIN_ID = "nl.jrdie.idea.springql"
    }

    override fun getReportActionText(): String {
        return QLBundle.getMessage("nl.jrdie.idea.springql.report.to.issue.tracker")
    }

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val event = events.firstOrNull()
        var title = "Exception: "
        var stacktrace = "Please paste the full stacktrace from the IDEA error popup\n"

        if (event != null) {
            val throwableText = event.throwableText
            val exceptionTitle: String = throwableText.lines().firstOrNull() ?: event.message
            title += exceptionTitle.ifBlank { "<Fill in title>" }
            if (!StringUtil.isEmptyOrSpaces(throwableText)) {
                val quotes = "\n```\n"
                stacktrace += quotes + StringUtil.first(throwableText, STACKTRACE_LENGTH, true) + quotes
            }
        }

        val plugin = PluginManagerCore.getPlugin(PluginId.getId(SPRING_GRAPHQL_SUPPORT_PLUGIN_ID))!!
        val dependencyInfo = plugin.dependencies.joinToString(separator = "\n") { "- ${it.pluginId} ${PluginManagerCore.getPlugin(it.pluginId)!!.version} ${if (it.isOptional) "OPTIONAL" else "REQUIRED"}" }

        val tpl = """
            |### IDE Error
            |$title
            |
            |### Description
            |${if (additionalInfo.isNullOrBlank()) "Please provide a description of what you were doing" else additionalInfo}
            |
            |### Stacktrace
            |$stacktrace
            |
            |### Technical Details
            |```
            |OS: ${SystemInfo.getOsNameAndVersion()} (${SystemInfo.OS_ARCH})
            |JVM: ${SystemInfo.JAVA_RUNTIME_VERSION} (${SystemInfo.JAVA_VERSION}) by ${SystemInfo.JAVA_VENDOR}
            |IDE: ${ApplicationInfo.getInstance().fullApplicationName} (${ApplicationInfo.getInstance().build})
            |Plugin version: ${plugin.version}
            |Dependencies:
            |$dependencyInfo
            |```
        """.trimMargin()
        val charset: Charset = StandardCharsets.UTF_8
        val url =
            "${GITHUB_ISSUE_URL}title=${URLEncoder.encode(title, charset)}&body=${URLEncoder.encode(tpl, charset)}"

        BrowserUtil.browse(url)
        consumer.consume(
            SubmittedReportInfo(
                null,
                "GitHub issue",
                SubmittedReportInfo.SubmissionStatus.NEW_ISSUE
            )
        )
        return true
    }
}

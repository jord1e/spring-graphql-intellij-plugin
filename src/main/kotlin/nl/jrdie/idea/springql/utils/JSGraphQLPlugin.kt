package nl.jrdie.idea.springql.utils

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

object JSGraphQLPlugin {

    private const val THE_2020_3_COMPAT_VERSION = "3.0.0-2020.3"
    private const val JS_GRAPHQL_PLUGIN_ID = "com.intellij.lang.jsgraphql"

    fun is2020dot3version(): Boolean {
        val jsGraphQLId = PluginId.getId(JS_GRAPHQL_PLUGIN_ID)
        return PluginManagerCore.getPlugin(jsGraphQLId)?.version == THE_2020_3_COMPAT_VERSION
    }
}

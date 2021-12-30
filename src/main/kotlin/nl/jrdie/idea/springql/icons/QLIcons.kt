package nl.jrdie.idea.springql.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object QLIcons {

    private fun getIcon(path: String): Icon {
        return IconLoader.getIcon(path, javaClass)
    }

    val Apollo = getIcon("/nl/jrdie/idea/springql/icons/apollo.svg")
    val BuildInDirective = getIcon("/nl/jrdie/idea/springql/icons/buildInDirective.svg")
    val Directive = getIcon("/nl/jrdie/idea/springql/icons/directive.svg")
    val Enum = getIcon("/nl/jrdie/idea/springql/icons/enum.svg")
    val Field = getIcon("/nl/jrdie/idea/springql/icons/field.svg")
    val Fragment = getIcon("/nl/jrdie/idea/springql/icons/fragment.svg")
    val Interface = getIcon("/nl/jrdie/idea/springql/icons/interface.svg")
    val IntrospectionFieldType = getIcon("/nl/jrdie/idea/springql/icons/introspectionFieldType.svg")
    val Mutation = getIcon("/nl/jrdie/idea/springql/icons/mutation.svg")
    val OperationVariable = getIcon("/nl/jrdie/idea/springql/icons/operationVariable.svg")
    val Query = getIcon("/nl/jrdie/idea/springql/icons/query.svg")
    val Scalar = getIcon("/nl/jrdie/idea/springql/icons/scalar.svg")
    val SchemaMappingMethod = getIcon("/nl/jrdie/idea/springql/icons/schemaMappingMethod.svg")
    val SpringGraphQL = getIcon("/nl/jrdie/idea/springql/icons/springGraphQL.svg")
    val SpringGraphGutterGreenQL = getIcon("/nl/jrdie/idea/springql/icons/springGraphQLGutterGreen.svg")
    val SpringGraphGutterGreyQL = getIcon("/nl/jrdie/idea/springql/icons/springGraphQLGutterGrey.svg")
    val Subscription = getIcon("/nl/jrdie/idea/springql/icons/subscription.svg")
    val Type = getIcon("/nl/jrdie/idea/springql/icons/type.svg")
    val Variable = getIcon("/nl/jrdie/idea/springql/icons/variable.svg")
}

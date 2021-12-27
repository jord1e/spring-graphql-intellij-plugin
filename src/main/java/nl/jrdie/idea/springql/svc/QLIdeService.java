package nl.jrdie.idea.springql.svc;

import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.openapi.project.Project;
import nl.jrdie.idea.springql.index.QLIdeIndex;
import nl.jrdie.idea.springql.types.SchemaMappingSummary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;

import java.util.List;

public interface QLIdeService {

    @NotNull
    default QLIdeIndex getIndex() {
        return getIndex(false);
    }

    @NotNull
    QLIdeIndex getIndex(boolean forceReload);

    boolean isApplicableProject(Project project);

    boolean isSchemaMappingAnnotation(UAnnotation uAnnotation);

    boolean isBatchMappingAnnotation(UAnnotation uAnnotation);

    boolean isValidBatchMappingReturnType(UMethod uMethod);

    boolean isMethodUsed(UMethod uMethod);

    boolean needsControllerAnnotation(@NotNull UClass uClass);

    boolean isIntrospectionNode(Node<?> node);

    boolean isApolloFederationSupportEnabled();

    @Nullable
    SchemaMappingSummary getSummaryForMethod(@NotNull UMethod uMethod);

    default boolean isApolloFederationNode(Node<?> node, boolean checkSupport) {
        if (checkSupport && !isApolloFederationSupportEnabled()) {
            return false;
        }
        return isApolloFederationNode(node);
    }

    boolean isApolloFederationNode(Node<?> node);

    @Nullable
    String findApplicableParentTypeName(@NotNull UAnnotation uAnnotation);

    List<UAnnotation> findNearestSchemaMappingAnnotations(UElement uElement);

    @NotNull
    QLSchemaRegistry getSchemaRegistry();

//    @NotNull
//    Project getApplicableProject();

}

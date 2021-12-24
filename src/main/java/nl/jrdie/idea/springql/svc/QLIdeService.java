package nl.jrdie.idea.springql.svc;

import com.intellij.openapi.project.Project;
import nl.jrdie.idea.springql.index.QLIdeIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UMethod;

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

    @NotNull
    QLSchemaRegistry getSchemaRegistry();

//    @NotNull
//    Project getApplicableProject();

}

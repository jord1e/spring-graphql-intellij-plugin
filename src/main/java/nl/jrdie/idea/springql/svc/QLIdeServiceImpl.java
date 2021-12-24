package nl.jrdie.idea.springql.svc;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeTracker;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.java.stubs.index.JavaStubIndexKeys;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiModificationTracker;
import nl.jrdie.idea.springql.index.MutableQLIdeIndex;
import nl.jrdie.idea.springql.index.QLIdeIndex;
import nl.jrdie.idea.springql.models.annotations.SchemaMappingType;
import nl.jrdie.idea.springql.services.QLAnnotationIndexProcessor;
import nl.jrdie.idea.springql.utils.QLIdeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class QLIdeServiceImpl implements QLIdeService, Disposable {

    private static final Set<String> SPRING_GRAPHQL_STANDARD_ANNOTATION_STUB_KEYS = Set.of(
            "SchemaMapping",
            "QueryMapping",
            "MutationMapping",
            "SubscriptionMapping"
//            "BatchMapping"
    );

    private static final Set<String> SPRING_GRAPHQL_STANDARD_ANNOTATION_FQN = Set.of(
            "org.springframework.graphql.data.method.SchemaMapping",
            "org.springframework.graphql.data.method.QueryMapping",
            "org.springframework.graphql.data.method.MutationMapping",
            "org.springframework.graphql.data.method.SubscriptionMapping"
//            "org.springframework.graphql.data.method.BatchMapping"
    );

    @NotNull
    private final Project project;

    @Nullable
    private QLSchemaRegistry cachedSchemaRegistry;

    @Nullable
    private QLIdeIndex cachedIdeIndex;

    @NotNull
    private final AtomicLong javaModificationCount;

    @NotNull
    private final AtomicLong kotlinModificationCount;

    @NotNull
    private final AtomicLong graphQLModificationCount;

    public QLIdeServiceImpl(Project project) {
        this.project = Objects.requireNonNull(project, "project");
        this.javaModificationCount = new AtomicLong(0);
        this.kotlinModificationCount = new AtomicLong(0);
        this.graphQLModificationCount = new AtomicLong(0);

        registerGraphQLSchemaChangeListener();
    }

    private void registerGraphQLSchemaChangeListener() {
        // TODO Check if this method is necessary.
        //  It seems like counting changes via GraphQLSchemaChangeTracker is enough (see #getIndex).

        // Automatically disposed when `this` service is disposed (we implement Disposable)
//        project.getMessageBus().connect(this).subscribe(
//                GraphQLSchemaChangeTracker.TOPIC,
//                () -> getIndex(true) // Force reloading of the index when a schema file changes
//        );
    }

    @NotNull
    @Override
    public QLIdeIndex getIndex(boolean forceReload) {
        // Only retrieve cached index if:
        //  1. Java, files were NOT modified
        //  2. Kotlin, files were NOT modified
        //  3. GraphQL, files were NOT modified
        //  4. `forceReload` is `false`
        //  5. A cached index is available
        if (!forceReload && this.cachedIdeIndex != null && !updateAndCheckModified()) {
            return this.cachedIdeIndex;
        }

        final StubIndex stubIndex = StubIndex.getInstance();

        final AtomicReference<MutableQLIdeIndex.MutableQLIdeIndexBuilder> indexBuilder = new AtomicReference<>(new MutableQLIdeIndex.MutableQLIdeIndexBuilder());
        final QLAnnotationIndexProcessor<MutableQLIdeIndex.MutableQLIdeIndexBuilder> processor = new QLAnnotationIndexProcessor<>(this);

        // Process standard annotations (Java)
        //  https://github.com/spring-projects/spring-graphql/tree/main/spring-graphql/src/main/java/org/springframework/graphql/data/method/annotation
        for (String stubKey : SPRING_GRAPHQL_STANDARD_ANNOTATION_STUB_KEYS) {
            stubIndex.processElements(JavaStubIndexKeys.ANNOTATIONS, stubKey, this.project, GlobalSearchScope.projectScope(this.project), PsiAnnotation.class, psiAnnotation -> {
                UAnnotation uAnnotation = UastContextKt.toUElement(psiAnnotation, UAnnotation.class);
                if (uAnnotation != null) {
                    indexBuilder.getAndUpdate(ib -> processor.process(uAnnotation, ib));
                }
                return true;
            });
        }

        final StubIndexKey<String, KtAnnotationEntry> ktAnnotationKey = KotlinAnnotationsIndex.getInstance().getKey();
        stubIndex.processAllKeys(ktAnnotationKey, this.project, annotationKey -> {
            if (!SPRING_GRAPHQL_STANDARD_ANNOTATION_STUB_KEYS.contains(annotationKey)) {
                return true;
            }

            StubIndex.getElements(ktAnnotationKey, annotationKey, this.project, GlobalSearchScope.projectScope(this.project), KtAnnotationEntry.class).forEach(annotationEntry -> {
                UAnnotation uAnnotation = UastContextKt.toUElement(annotationEntry, UAnnotation.class);
                if (uAnnotation != null) {
                    indexBuilder.getAndUpdate(ib -> processor.process(uAnnotation, ib));
                }
            });

            return true;
        });

        // TODO process non-standard annotations (Spring meta-annotations)

        // TODO Is refreshing necessary?
        ProjectView.getInstance(project).refresh();

        return this.cachedIdeIndex = indexBuilder.get().build();
    }

    @Override
    public boolean isApplicableProject(Project project) {
        //noinspection PointlessNullCheck
        return project != null && this.project.equals(project);
    }

    @Override
    public boolean isSchemaMappingAnnotation(UAnnotation uAnnotation) {
        // TODO Implement Spring annotation model
        return SchemaMappingType.Companion.isSchemaMappingAnnotation(uAnnotation.getQualifiedName());
    }

    @Override
    public boolean isBatchMappingAnnotation(UAnnotation uAnnotation) {
        return QLIdeUtil.INSTANCE.isBatchMappingAnnotation(uAnnotation);
    }

    @Override
    public boolean isValidBatchMappingReturnType(UMethod uMethod) {
        // Logic can be found over at:
        //  https://github.com/spring-projects/spring-graphql/blob/a15fd29fc0f95b675ba13c15f7d825e97511f527/spring-graphql/src/main/java/org/springframework/graphql/data/method/annotation/support/AnnotatedControllerConfigurer.java#L328-L352

        final PsiType returnType = uMethod.getReturnType();
        if (returnType == null) {
            return false;
        }

        final GlobalSearchScope searchScope = GlobalSearchScope.allScope(this.project);

        final PsiType collectionType = PsiType.getTypeByName(CommonClassNames.JAVA_UTIL_COLLECTION, this.project, searchScope);
        if (returnType.isAssignableFrom(collectionType)) {
            return true;
        }

        final PsiType mapType = PsiType.getTypeByName(CommonClassNames.JAVA_UTIL_MAP, this.project, searchScope);
        if (returnType.isAssignableFrom(mapType)) {
            return true;
        }

        final PsiType monoType = PsiType.getTypeByName("reactor.core.publisher.Mono", this.project, searchScope);
        if (returnType.isAssignableFrom(monoType)) {
            return true;
        }

        final PsiType fluxType = PsiType.getTypeByName("reactor.core.publisher.Flux", this.project, searchScope);
        //noinspection RedundantIfStatement
        if (returnType.isAssignableFrom(fluxType)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isMethodUsed(UMethod uMethod) {
        return uMethod.getUAnnotations()
                .stream()
                .anyMatch(uAnnotation -> isSchemaMappingAnnotation(uAnnotation) ||
                        isBatchMappingAnnotation(uAnnotation) ||
                        SPRING_GRAPHQL_STANDARD_ANNOTATION_FQN.contains(uAnnotation.getQualifiedName()));
    }

    @NotNull
    @Override
    public QLSchemaRegistry getSchemaRegistry() {
        return new QLSchemaRegistry(getTypeDefinitionRegistry());
//        return Objects.requireNonNullElseGet(cachedSchemaRegistry,
//                () -> cachedSchemaRegistry = new QLSchemaRegistry(getTypeDefinitionRegistry()));
    }

    @NotNull
    private TypeDefinitionRegistry getTypeDefinitionRegistry() {
        final PsiElement rootPsi = getProjectRootPsi();
        return GraphQLSchemaProvider.getInstance(project)
                .getRegistryInfo(rootPsi)
                .getTypeDefinitionRegistry();
    }

    @NotNull
    private GraphQLSchemaInfo getGraphQLSchemaInfo() {
        final PsiElement rootPsi = getProjectRootPsi();
        return GraphQLSchemaProvider.getInstance(project)
                .getSchemaInfo(rootPsi);
    }

    @NotNull
    private PsiElement getProjectRootPsi() {
        final VirtualFile projectFile = Objects.requireNonNull(project.getProjectFile(),
                "projectFile is null for '" + project.getName() + "'");
        return Objects.requireNonNull(PsiManager.getInstance(project).findFile(projectFile),
                "rootPsi is null for project '" + project.getName() + "' at '" + projectFile + "'");
    }

    private boolean updateAndCheckModified() {
        // TODO Think about tracking changes in Groovy and Scala files.
        //  Requirement for this are that UAST is supported, and the languages are actually being used with Spring GraphQL.
        //  A combination of Groovy, Spock, and Spring GraphQL would probably be a good use case.

        long javaModificationCount = PsiModificationTracker.SERVICE
                .getInstance(this.project)
                .forLanguage(JavaLanguage.INSTANCE)
                .getModificationCount();

        boolean javaModified = this.javaModificationCount.getAndSet(javaModificationCount) != javaModificationCount;

        long kotlinModificationCount = PsiModificationTracker.SERVICE
                .getInstance(this.project)
                .forLanguage(JavaLanguage.INSTANCE)
                .getModificationCount();

        boolean kotlinModified = this.kotlinModificationCount.getAndSet(kotlinModificationCount) != kotlinModificationCount;

        long graphQLModificationCount = GraphQLSchemaChangeTracker
                .getInstance(this.project)
                .getSchemaModificationTracker()
                .getModificationCount();

        boolean graphQLModified = this.graphQLModificationCount.getAndSet(graphQLModificationCount) != graphQLModificationCount;

        return javaModified || kotlinModified || graphQLModified;
    }

    @Override
    public void dispose() {
        // Do nothing.
    }

}

package nl.jrdie.idea.springql.svc;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
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
import com.intellij.util.messages.Topic;
import nl.jrdie.idea.springql.GraphQLSchemaEventListener;
import nl.jrdie.idea.springql.index.MutableQLIdeIndex;
import nl.jrdie.idea.springql.index.QLIdeIndex;
import nl.jrdie.idea.springql.index.entry.QLClassSchemaMappingIndexEntry;
import nl.jrdie.idea.springql.index.entry.QLMethodSchemaMappingIndexEntry;
import nl.jrdie.idea.springql.index.processor.QLAnnotationIndexProcessor;
import nl.jrdie.idea.springql.types.SchemaMappingSummary;
import nl.jrdie.idea.springql.utils.JSGraphQLPlugin;
import nl.jrdie.idea.springql.utils.JSGraphQLVersionBypassUtils;
import nl.jrdie.idea.springql.utils.QLIdeUtil;
import nl.jrdie.idea.springql.utils.UExtKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.ULiteralExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class QLIdeServiceImpl implements QLIdeService, Disposable {

  private static final Logger LOGGER = Logger.getInstance(QLIdeServiceImpl.class);

  private static final Set<String> SPRING_GRAPHQL_STANDARD_ANNOTATION_STUB_KEYS =
      Set.of(
          "SchemaMapping",
          "QueryMapping",
          "MutationMapping",
          "SubscriptionMapping",
          "BatchMapping");

  private static final Set<String> SPRING_GRAPHQL_STANDARD_ANNOTATION_FQN =
      Set.of(
          "org.springframework.graphql.data.method.SchemaMapping",
          "org.springframework.graphql.data.method.QueryMapping",
          "org.springframework.graphql.data.method.MutationMapping",
          "org.springframework.graphql.data.method.SubscriptionMapping",
          "org.springframework.graphql.data.method.BatchMapping");

  // TODO Implement Spring meta-annotations.
  private static final String SPRING_CONTROLLER_FQN = "org.springframework.stereotype.Controller";

  private static final NumberFormat ENGLISH_NUMBER_FORMAT =
      NumberFormat.getIntegerInstance(Locale.ENGLISH);

  @NotNull private final Project project;

  // TODO  @Nullable private QLSchemaRegistry cachedSchemaRegistry;

  @Nullable private QLIdeIndex cachedIdeIndex;

  @NotNull private final AtomicLong javaModificationCount;

  @NotNull private final AtomicLong kotlinModificationCount;

  @NotNull private final AtomicLong graphQLModificationCount;

  public QLIdeServiceImpl(Project project) {
    this.project = Objects.requireNonNull(project, "project");
    this.javaModificationCount = new AtomicLong(0);
    this.kotlinModificationCount = new AtomicLong(0);
    this.graphQLModificationCount = new AtomicLong(0);

    tryRegisterGraphQLSchemaChangeListener();
  }

  @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
  private void tryRegisterGraphQLSchemaChangeListener() {
    // Only implement this for the 2020.3 compatible JS GraphQL version (3.0.0-2020.3)
    if (!JSGraphQLPlugin.INSTANCE.is2020dot3version()) {
      return;
    }

    // ----------------------------------------
    // I DISGUST EVERYTHING ABOUT THIS FUNCTION
    // ----------------------------------------

    // https://github.com/jimkyndemeyer/js-graphql-intellij-plugin/blob/3.0.0/src/main/com/intellij/lang/jsgraphql/schema/GraphQLSchemaChangeListener.java
    // com.intellij.lang.jsgraphql.schema
    try {
      Class<?> graphQLSchemaChangeListenerCls =
          Class.forName("com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeListener");
      Topic<? super GraphQLSchemaEventListener> targetTopic =
          (Topic<? super GraphQLSchemaEventListener>)
              graphQLSchemaChangeListenerCls.getField("TOPIC").get(null);

      if (targetTopic == null) {
        throw new IllegalStateException("TOPIC Not found"); // TODO Error message
      }

      // Automatically disposed when `this` service is disposed (we implement Disposable)
      project
          .getMessageBus()
          .connect(this)
          .subscribe(
              targetTopic,
              (GraphQLSchemaEventListener)
                  schemaVersion -> {
                    // Force reloading of the index when a schema file changes
                    QLIdeServiceImpl.this.getIndex(true);
                  });
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to register schema change listener for 2020.3 JS GraphQL plugin version @ GraphQL Spring Support",
          e);
    }
  }

  @NotNull
  @Override
  public QLIdeIndex getIndex(boolean forceReload) {
    if (DumbService.isDumb(this.project)) {
      return Objects.requireNonNullElseGet(
          this.cachedIdeIndex, () -> new MutableQLIdeIndex.MutableQLIdeIndexBuilder().build());
    }

    // Only retrieve cached index if:
    //  1. Java, files were NOT modified
    //  2. Kotlin, files were NOT modified
    //  3. GraphQL, files were NOT modified
    //  4. `forceReload` is `false`
    //  5. A cached index is available
    if (!forceReload && this.cachedIdeIndex != null && !updateAndCheckModified()) {
      return this.cachedIdeIndex;
    }

    final long startNanos = System.nanoTime();

    final StubIndex stubIndex = StubIndex.getInstance();

    final AtomicReference<MutableQLIdeIndex.MutableQLIdeIndexBuilder> indexBuilder =
        new AtomicReference<>(new MutableQLIdeIndex.MutableQLIdeIndexBuilder());
    final QLAnnotationIndexProcessor<MutableQLIdeIndex.MutableQLIdeIndexBuilder> processor =
        new QLAnnotationIndexProcessor<>(this);

    // Process standard annotations (Java)
    //
    // https://github.com/spring-projects/spring-graphql/tree/main/spring-graphql/src/main/java/org/springframework/graphql/data/method/annotation
    final GlobalSearchScope searchScope = GlobalSearchScope.projectScope(this.project);
    for (String stubKey : SPRING_GRAPHQL_STANDARD_ANNOTATION_STUB_KEYS) {
      stubIndex.processElements(
          JavaStubIndexKeys.ANNOTATIONS,
          stubKey,
          this.project,
          searchScope,
          PsiAnnotation.class,
          psiAnnotation -> {
            UAnnotation uAnnotation = UastContextKt.toUElement(psiAnnotation, UAnnotation.class);
            if (uAnnotation != null) {
              indexBuilder.getAndUpdate(ib -> processor.process(uAnnotation, ib));
            }
            return true;
          });
    }

    final StubIndexKey<String, KtAnnotationEntry> ktAnnotationKey =
        KotlinAnnotationsIndex.getInstance().getKey();
    stubIndex.processAllKeys(
        ktAnnotationKey,
        this.project,
        annotationKey -> {
          if (!SPRING_GRAPHQL_STANDARD_ANNOTATION_STUB_KEYS.contains(annotationKey)) {
            return true;
          }

          StubIndex.getElements(
                  ktAnnotationKey,
                  annotationKey,
                  this.project,
                  searchScope,
                  KtAnnotationEntry.class)
              .forEach(
                  annotationEntry -> {
                    UAnnotation uAnnotation =
                        UastContextKt.toUElement(annotationEntry, UAnnotation.class);
                    if (uAnnotation != null) {
                      indexBuilder.getAndUpdate(ib -> processor.process(uAnnotation, ib));
                    }
                  });

          return true;
        });

    // TODO process non-standard annotations (Spring meta-annotations)

    final Duration indexTime = Duration.ofNanos(System.nanoTime() - startNanos);
    LOGGER.info(
        "Spring GraphQL Support "
            + (forceReload ? "forceful " : "")
            + "indexing took "
            + indexTime.toMillis()
            + " ms ("
            + ENGLISH_NUMBER_FORMAT.format(indexTime.toNanos())
            + " ns)");

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
    return QLIdeUtil.INSTANCE.isDefaultSchemaMappingAnnotation(uAnnotation);
  }

  @Override
  public boolean isBatchMappingAnnotation(UAnnotation uAnnotation) {
    return QLIdeUtil.INSTANCE.isBatchMappingAnnotation(uAnnotation);
  }

  @Override
  public boolean isValidBatchMappingReturnType(UMethod uMethod) {
    // Logic can be found over at:
    //
    // https://github.com/spring-projects/spring-graphql/blob/a15fd29fc0f95b675ba13c15f7d825e97511f527/spring-graphql/src/main/java/org/springframework/graphql/data/method/annotation/support/AnnotatedControllerConfigurer.java#L328-L352

    final PsiType returnType = uMethod.getReturnType();
    if (returnType == null) {
      return false;
    }

    final GlobalSearchScope searchScope = GlobalSearchScope.allScope(this.project);

    final PsiType collectionType =
        PsiType.getTypeByName(CommonClassNames.JAVA_UTIL_COLLECTION, this.project, searchScope);
    if (collectionType.isAssignableFrom(returnType)) {
      return true;
    }

    final PsiType mapType =
        PsiType.getTypeByName(CommonClassNames.JAVA_UTIL_MAP, this.project, searchScope);
    if (mapType.isAssignableFrom(returnType)) {
      return true;
    }

    // TODO This only handles Mono<V>, but it should check for Mono<Map<K, V>>.
    final PsiType monoType =
        PsiType.getTypeByName("reactor.core.publisher.Mono", this.project, searchScope);
    if (monoType.isAssignableFrom(returnType)) {
      return true;
    }

    final PsiType fluxType =
        PsiType.getTypeByName("reactor.core.publisher.Flux", this.project, searchScope);
    //noinspection RedundantIfStatement
    if (fluxType.isAssignableFrom(returnType)) {
      return true;
    }

    return false;
  }

  @Override
  public boolean isMethodUsed(UMethod uMethod) {
    return uMethod.getUAnnotations().stream()
        .anyMatch(
            uAnnotation ->
                isSchemaMappingAnnotation(uAnnotation)
                    || isBatchMappingAnnotation(uAnnotation)
                    || SPRING_GRAPHQL_STANDARD_ANNOTATION_FQN.contains(
                        uAnnotation.getQualifiedName()));
  }

  @Override
  public boolean needsControllerAnnotation(@NotNull UClass uClass) {
    Objects.requireNonNull(uClass, "uClass");

    if (UExtKt.hasUAnnotation(uClass, SPRING_CONTROLLER_FQN)) {
      return false;
    }

    //noinspection UnnecessaryLocalVariable
    final boolean containsAnnotations =
        uClass.getUAnnotations().stream()
            .anyMatch(
                uAnnotation ->
                    isSchemaMappingAnnotation(uAnnotation)
                        || isBatchMappingAnnotation(uAnnotation));

    return containsAnnotations;
  }

  @Override
  public boolean isIntrospectionNode(Node<?> node) {
    if (node instanceof ObjectTypeDefinition) {
      final ObjectTypeDefinition typeDefinition = (ObjectTypeDefinition) node;
      return typeDefinition.getName().startsWith("__");
    }
    if (node instanceof FieldDefinition) {
      return getSchemaRegistry().getObjectDefinitions().stream()
          .filter(typeDefinition -> typeDefinition.getName().startsWith("__"))
          .anyMatch(typeDefinition -> typeDefinition.getFieldDefinitions().contains(node));
    }
    return false;
  }

  private boolean hasFieldWithNameAndType(
      @NotNull ObjectTypeDefinition typeDefinition,
      @NotNull String fieldName,
      @NotNull String fieldType) {
    Objects.requireNonNull(typeDefinition, "typeDefinition");
    Objects.requireNonNull(fieldName, "fieldName");
    Objects.requireNonNull(fieldType, "fieldType");
    return typeDefinition.getFieldDefinitions().stream()
        .anyMatch(
            fieldDefinition ->
                fieldDefinition != null
                    && fieldDefinition.getType() != null
                    && fieldName.equals(fieldDefinition.getName())
                    && fieldType.equals(TypeUtil.simplePrint(fieldDefinition.getType())));
  }

  @Override
  public boolean isApolloFederationSupportEnabled() {
    return !JSGraphQLPlugin.INSTANCE.is2020dot3version()
        && JSGraphQLVersionBypassUtils.isApolloFederationEnabled(this.project);
  }

  @Nullable
  @Override
  public SchemaMappingSummary getSummaryForMethod(@NotNull UMethod uMethod) {
    Objects.requireNonNull(uMethod, "uMethod");

    QLIdeIndex index = getIndex();
    List<QLMethodSchemaMappingIndexEntry> a = index.methodSchemaMappingByMethod(uMethod);

    if (a.isEmpty()) {
      return null;
    }

    if (a.size() > 1) {
      throw new IllegalStateException("Should not happen");
    }

    QLMethodSchemaMappingIndexEntry b = a.get(0);

    String typeName = b.getParentType();
    if (typeName == null || typeName.isEmpty()) {
      UClass uClass = UastContextKt.getUastParentOfType(uMethod.getSourcePsi(), UClass.class);
      if (uClass != null) {
        Set<QLClassSchemaMappingIndexEntry> c = index.schemaMappingByClass(uClass);

        if (c.size() > 1) {
          throw new IllegalStateException(
              "Should not happen - more then one Class -> @SchemaMapping mapping");
        }
        if (!c.isEmpty()) {
          QLClassSchemaMappingIndexEntry only = c.iterator().next(); // TODO Convert index to list
          typeName = only.getParentType();
        }
        // No parent
        // TODO Check if parentType || typeName is null?
      } else {
        // TODO No parent type - what should we do?
      }
    }

    if (typeName == null || typeName.isEmpty()) {
      typeName = ""; // TODO
    }

    String fieldName = b.getField();
    if (fieldName == null || fieldName.isEmpty()) {
      fieldName = uMethod.getName(); // todo null check?
    }

    PsiElement schemaPsi = getSchemaRegistry().getSchemaPsiForObject(typeName, fieldName);

    return new SchemaMappingSummary(
        typeName,
        fieldName,
        b.getAnnotationPsi(),
        schemaPsi,
        b.getUAnnotation(),
        QLIdeUtil.INSTANCE.reduceSchemaMappingAnnotationName(b.getUAnnotation()));
  }

  @Override
  public boolean isApolloFederationNode(Node<?> node) {
    // See:
    // https://github.com/jimkyndemeyer/js-graphql-intellij-plugin/blob/329fc22458a474ae807cd1d71d62d36b387392fc/resources/definitions/Federation.graphql
    if (node instanceof FieldDefinition) {
      FieldDefinition fieldDefinition = (FieldDefinition) node;
      // sdl: String
      if ("sdl".equals(fieldDefinition.getName())) {
        ObjectTypeDefinition parentType = getSchemaRegistry().getParentType(fieldDefinition);
        return (parentType != null && parentType.getName().equals("_Service"))
            || "String".equals(TypeUtil.simplePrint(fieldDefinition.getType()));
      }
      // _service: _Service!
      if ("_service".equals(fieldDefinition.getName())) {
        return "_Service".equals(TypeUtil.simplePrint(fieldDefinition.getType()));
      }
      // _entities(representations: [_Any!]!): [_Entity]!
      if ("_entities".equals(fieldDefinition.getName())) {
        final List<InputValueDefinition> inputValueDefinitions =
            fieldDefinition.getInputValueDefinitions();
        if (inputValueDefinitions.size() != 1) {
          return false;
        }
        final InputValueDefinition firstDefinition = inputValueDefinitions.get(0);
        if (firstDefinition == null
            || !"representation".equals(firstDefinition.getName())
            || !"[_Any!]!".equals(TypeUtil.simplePrint(firstDefinition.getType()))) {
          return false;
        }
        return "[_Entity]!".equals(TypeUtil.simplePrint(fieldDefinition.getType()));
      }
    }
    if (node instanceof ObjectTypeDefinition) {
      ObjectTypeDefinition typeDefinition = (ObjectTypeDefinition) node;
      if (typeDefinition.getName().equals("_Service")) {
        return hasFieldWithNameAndType(typeDefinition, "sdl", "String");
      }
    }
    return false;
  }

  @Nullable
  @Override
  public String findApplicableParentTypeName(@NotNull UAnnotation uAnnotation) {
    Objects.requireNonNull(uAnnotation, "uAnnotation");

    return QLIdeUtil.INSTANCE.getSchemaMappingTypeName(uAnnotation);
  }

  @Override
  public List<UAnnotation> findNearestSchemaMappingAnnotations(UElement uElement) {
    // I have no idea what I am doing in this method.

    if (uElement instanceof UAnnotation) {
      final UAnnotation uAnnotation = (UAnnotation) uElement;
      if (isSchemaMappingAnnotation(uAnnotation)) {
        return Collections.singletonList(uAnnotation);
      } else {
        return Collections.emptyList();
      }
    }

    if (uElement instanceof UMethod) {
      final UMethod uMethod = (UMethod) uElement;
      return uMethod.getUAnnotations().stream()
          .filter(Objects::nonNull)
          .filter(this::isSchemaMappingAnnotation)
          .collect(Collectors.toUnmodifiableList());
    }

    if (uElement instanceof UClass) {
      final UClass uClass = (UClass) uElement;
      return uClass.getUAnnotations().stream()
          .filter(Objects::nonNull)
          .filter(this::isSchemaMappingAnnotation)
          .collect(Collectors.toUnmodifiableList());
    }

    if (uElement instanceof ULiteralExpression) {
      final ULiteralExpression uLiteralExpression = (ULiteralExpression) uElement;
      final UAnnotation uAnnotation =
          UastContextKt.getUastParentOfType(uLiteralExpression.getSourcePsi(), UAnnotation.class);

      if (uAnnotation != null && isSchemaMappingAnnotation(uAnnotation)) {
        return Collections.singletonList(uAnnotation);
      } else {
        return Collections.emptyList();
      }
    }

    return null;
  }

  @NotNull
  @Override
  public QLSchemaRegistry getSchemaRegistry() {
    // TODO Cache registry?
    return new QLSchemaRegistry(getTypeDefinitionRegistry(), getGraphQLSchemaInfo());
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
    return GraphQLSchemaProvider.getInstance(project).getSchemaInfo(rootPsi);
  }

  @NotNull
  private PsiElement getProjectRootPsi() {
    final VirtualFile projectFile =
        Objects.requireNonNull(
            project.getProjectFile(), "projectFile is null for '" + project.getName() + "'");
    return Objects.requireNonNull(
        PsiManager.getInstance(project).findFile(projectFile),
        "rootPsi is null for project '" + project.getName() + "' at '" + projectFile + "'");
  }

  private boolean updateAndCheckModified() {
    // TODO Think about tracking changes in Groovy and Scala files.
    //  Requirement for this are that UAST is supported, and the languages are actually being used
    // with Spring GraphQL.
    //  A combination of Groovy, Spock, and Spring GraphQL would probably be a good use case.

    long javaModificationCount =
        PsiModificationTracker.SERVICE
            .getInstance(this.project)
            .forLanguage(JavaLanguage.INSTANCE)
            .getModificationCount();

    boolean javaModified =
        this.javaModificationCount.getAndSet(javaModificationCount) != javaModificationCount;

    long kotlinModificationCount =
        PsiModificationTracker.SERVICE
            .getInstance(this.project)
            .forLanguage(JavaLanguage.INSTANCE)
            .getModificationCount();

    boolean kotlinModified =
        this.kotlinModificationCount.getAndSet(kotlinModificationCount) != kotlinModificationCount;

    boolean graphQLModified = false;

    // TODO Fix this 2020.3 support logic
    if (!JSGraphQLPlugin.INSTANCE.is2020dot3version()) {
      //noinspection CommentedOutCode
      try {
        Class<?> changeTracker =
            Class.forName("com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeTracker");
        Object schemaTracker =
            changeTracker.getMethod("getInstance", Project.class).invoke(null, this.project);
        Object modificationTracker =
            schemaTracker
                .getClass()
                .getMethod("getSchemaModificationTracker")
                .invoke(schemaTracker);
        long graphQLModificationCount =
            (long)
                schemaTracker
                    .getClass()
                    .getMethod("getModificationCount")
                    .invoke(modificationTracker);

        //        long graphQLModificationCount =
        //            GraphQLSchemaChangeTracker.getInstance(this.project)
        //                .getSchemaModificationTracker()
        //                .getModificationCount();

        graphQLModified =
            this.graphQLModificationCount.getAndSet(graphQLModificationCount)
                != graphQLModificationCount;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return javaModified || kotlinModified || graphQLModified;
  }

  @Override
  public void dispose() {
    // Do nothing.
  }
}

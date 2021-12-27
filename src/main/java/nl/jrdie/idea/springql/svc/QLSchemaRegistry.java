package nl.jrdie.idea.springql.svc;

import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.types.language.AbstractNode;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QLSchemaRegistry {

    @NotNull
    private final TypeDefinitionRegistry typeDefinitionRegistry;

    @NotNull
    private final GraphQLSchemaInfo graphQLSchemaInfo;

    public QLSchemaRegistry(TypeDefinitionRegistry typeDefinitionRegistry, GraphQLSchemaInfo graphQLSchemaInfo) {
        this.typeDefinitionRegistry = Objects.requireNonNull(typeDefinitionRegistry, "typeDefinitionRegistry");
        this.graphQLSchemaInfo = Objects.requireNonNull(graphQLSchemaInfo, "graphQLSchemaInfo");
    }

    public boolean hasObjectWithExactTypeName(@NotNull String typeName) {
        Objects.requireNonNull(typeName, "typeName");

        return typeDefinitionRegistry
                .getType(typeName, ObjectTypeDefinition.class)
                .isPresent();
    }

    @Nullable
    public PsiElement getSchemaPsiForObject(@NotNull String typeName, @NotNull String field) {
        Objects.requireNonNull(typeName, "typeName");
        Objects.requireNonNull(field, "field");

        return typeDefinitionRegistry
                .getType(typeName, ObjectTypeDefinition.class)
                .flatMap(type -> type.getFieldDefinitions()
                        .stream()
                        .filter(fieldDefinition -> field.equals(fieldDefinition.getName()))
                        .findFirst())
                .map(AbstractNode::getSourceLocation)
                .map(SourceLocation::getElement)
                .orElse(null);
    }

    @NotNull
    public List<PsiElement> getAllSchemaPsiForObject(@NotNull String typeName, @NotNull String field) {
        Objects.requireNonNull(typeName, "typeName");
        Objects.requireNonNull(field, "field");

        return typeDefinitionRegistry
                .getType(typeName, ObjectTypeDefinition.class)
                .map(type -> type.getFieldDefinitions()
                        .stream()
                        .filter(fieldDefinition -> field.equals(fieldDefinition.getName()))
                        .map(AbstractNode::getSourceLocation)
                        .map(SourceLocation::getElement)
                        .filter(Objects::nonNull)
//                        .flatMap(List::stream)
                        .collect(Collectors.toUnmodifiableList()))
                .orElse(Collections.emptyList());
    }

    @Nullable
    public PsiElement getSchemaPsiForObject(@NotNull String typeName) {
        Objects.requireNonNull(typeName, "typeName");

        return typeDefinitionRegistry
                .getType(typeName, ObjectTypeDefinition.class)
                .map(AbstractNode::getElement)
                .orElse(null);
    }

    @Nullable
    public ObjectTypeDefinition getObjectTypeDefinition(@NotNull String typeName) {
        Objects.requireNonNull(typeName, "typeName");

        return typeDefinitionRegistry
                .getType(typeName, ObjectTypeDefinition.class)
                .orElse(null);
    }

    @NotNull
    public List<ObjectTypeDefinition> getObjectDefinitions() {
        return Collections.unmodifiableList(typeDefinitionRegistry.getTypes(ObjectTypeDefinition.class));
    }

    @NotNull
    public List<FieldDefinition> getFieldDefinitions() {
        return getObjectDefinitions()
                .stream()
                .map(ObjectTypeDefinition::getFieldDefinitions)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }

    @Nullable
    public ObjectTypeDefinition getParentType(@NotNull FieldDefinition fieldDefinition) {
        Objects.requireNonNull(fieldDefinition, "fieldDefinition");

        return getObjectDefinitions()
                .stream()
                .filter(objectTypeDefinition -> objectTypeDefinition.getFieldDefinitions().contains(fieldDefinition))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public GraphQLSchemaInfo getGraphQLSchemaInfo() {
        return graphQLSchemaInfo;
    }
}

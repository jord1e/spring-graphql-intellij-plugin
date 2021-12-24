package nl.jrdie.idea.springql.svc;

import com.intellij.lang.jsgraphql.types.language.AbstractNode;
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

    public QLSchemaRegistry(TypeDefinitionRegistry typeDefinitionRegistry) {
        this.typeDefinitionRegistry = Objects.requireNonNull(typeDefinitionRegistry, "typeDefinitionRegistry");
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
//                        .flatMap(List::stream)
                        .collect(Collectors.toList()))
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

    @NotNull
    public List<ObjectTypeDefinition> getObjectDefinitions() {
        return Collections.unmodifiableList(typeDefinitionRegistry.getTypes(ObjectTypeDefinition.class));
    }

}

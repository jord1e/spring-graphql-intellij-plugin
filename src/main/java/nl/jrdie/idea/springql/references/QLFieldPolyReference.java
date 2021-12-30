package nl.jrdie.idea.springql.references;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.Icon;
import nl.jrdie.idea.springql.icons.QLIcons;
import nl.jrdie.idea.springql.ide.codeInsight.completion.QLForeignFieldNameInsertHandler;
import nl.jrdie.idea.springql.index.entry.SchemaMappingIndexEntry;
import nl.jrdie.idea.springql.svc.QLIdeService;
import nl.jrdie.idea.springql.utils.QLIdeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;

public class QLFieldPolyReference extends PsiPolyVariantReferenceBase<PsiElement> {

  public QLFieldPolyReference(@NotNull PsiElement psiElement) {
    super(Objects.requireNonNull(psiElement, "psiElement"));
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final QLIdeService svc = myElement.getProject().getService(QLIdeService.class);
    if (!svc.isApplicableProject(myElement.getProject())) {
      return ResolveResult.EMPTY_ARRAY;
    }

    final UAnnotation parentAnnotation =
        UastContextKt.getUastParentOfType(myElement, UAnnotation.class);
    if (parentAnnotation == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    final Set<SchemaMappingIndexEntry> annotations =
        svc.getIndex().schemaMappingByAnnotation(parentAnnotation);

    return annotations.stream()
        .map(SchemaMappingIndexEntry::getSchemaPsi)
        .flatMap(List::stream)
        .map(PsiElementResolveResult::new)
        .toArray(ResolveResult[]::new);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final QLIdeService svc = myElement.getProject().getService(QLIdeService.class);

    return svc.getSchemaRegistry().getFieldDefinitions().stream()
        .map(fieldDefinition -> createLookupElement(svc, fieldDefinition))
        .filter(Objects::nonNull)
        .toArray(LookupElement[]::new);
  }

  @Nullable
  private LookupElement createLookupElement(
      @NotNull QLIdeService svc, @NotNull FieldDefinition fieldDefinition) {
    Objects.requireNonNull(svc, "svc");
    Objects.requireNonNull(fieldDefinition, "fieldDefinition");

    final PsiElement targetElement = fieldDefinition.getElement();
    if (targetElement == null) {
      return null;
    }

    final UAnnotation nearestAnnotation =
        svc.findNearestSchemaMappingAnnotations(UastContextKt.toUElement(myElement)).stream()
            .findFirst()
            .orElse(null);

    ObjectTypeDefinition parentTypeByAnnotation = null;
    if (nearestAnnotation != null) {
      final String parentTypeName = svc.findApplicableParentTypeName(nearestAnnotation);
      if (parentTypeName == null) {
        return null;
      }

      parentTypeByAnnotation = svc.getSchemaRegistry().getObjectTypeDefinition(parentTypeName);
    }

    final boolean isChildOfCurrentAnnotationParentType =
        parentTypeByAnnotation != null
            && parentTypeByAnnotation.getFieldDefinitions().contains(fieldDefinition);

    String correctDisplayName = null;
    if (isChildOfCurrentAnnotationParentType) {
      correctDisplayName = fieldDefinition.getName();
    } else {
      final ObjectTypeDefinition parentTypeDefinition =
          svc.getSchemaRegistry().getParentType(fieldDefinition);
      if (parentTypeDefinition != null) {
        correctDisplayName = parentTypeDefinition.getName() + ".";
      }
      correctDisplayName += fieldDefinition.getName();
    }

    final String typeText = QLIdeUtil.printTypeVal(fieldDefinition.getType());

    final Icon icon = getLookupIcon(svc, fieldDefinition);

    final LookupElement intermediateLookupElement =
        LookupElementBuilder.create(fieldDefinition.getName())
            .withPresentableText(correctDisplayName)
            .withPsiElement(fieldDefinition.getElement())
            .withIcon(icon)
            .withBoldness(isChildOfCurrentAnnotationParentType)
            .withStrikeoutness(fieldDefinition.hasDirective("deprecated"))
            .withInsertHandler(new QLForeignFieldNameInsertHandler(fieldDefinition))
            .withTypeText(typeText);

    return PrioritizedLookupElement.withPriority(
        intermediateLookupElement, determinePriority(fieldDefinition, parentTypeByAnnotation, svc));
  }

  private Icon getLookupIcon(@NotNull QLIdeService svc, @NotNull FieldDefinition fieldDefinition) {
    Objects.requireNonNull(svc, "svc");
    Objects.requireNonNull(fieldDefinition, "fieldDefinition");

    if (svc.isApolloFederationNode(fieldDefinition)) {
      return QLIcons.INSTANCE.getApollo();
    }
    if (svc.isIntrospectionNode(fieldDefinition)) {
      return QLIcons.INSTANCE.getIntrospectionFieldType();
    }
    return QLIcons.INSTANCE.getField();
  }

  private double determinePriority(
      @NotNull FieldDefinition fieldDefinition,
      @Nullable ObjectTypeDefinition parentType,
      @NotNull QLIdeService svc) {
    Objects.requireNonNull(fieldDefinition, "fieldDefinition");
    Objects.requireNonNull(svc, "svc");

    if (svc.isIntrospectionNode(fieldDefinition)) {
      return 0.0;
    }

    if (fieldDefinition.hasDirective("deprecated")) {
      return 7.0;
    }

    UMethod nearestMethod = UastContextKt.getUastParentOfType(myElement, UMethod.class);
    if (nearestMethod != null) {
      if (fieldDefinition.getName().equalsIgnoreCase(nearestMethod.getName())) {
        return 10.0;
      }
    }

    if (parentType != null && parentType.getFieldDefinitions().contains(fieldDefinition)) {
      return 8.0;
    }

    return 5.0;
  }
}

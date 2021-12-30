package nl.jrdie.idea.springql.references;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ArrayUtilRt;
import java.util.Objects;
import javax.swing.Icon;
import nl.jrdie.idea.springql.icons.QLIcons;
import nl.jrdie.idea.springql.index.entry.SchemaMappingIndexEntry;
import nl.jrdie.idea.springql.svc.QLIdeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UastContextKt;

public class QLTypeNamePolyReference extends PsiPolyVariantReferenceBase<PsiElement> {

  public QLTypeNamePolyReference(@NotNull PsiElement psiElement) {
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

    return svc.getIndex().schemaMappingByAnnotation(parentAnnotation).stream()
        .map(SchemaMappingIndexEntry::getParentType)
        .filter(Objects::nonNull)
        .map(svc.getSchemaRegistry()::getObjectTypeDefinition)
        .filter(Objects::nonNull)
        .map(ObjectTypeDefinition::getElement)
        .filter(Objects::nonNull)
        .map(PsiElementResolveResult::new)
        .toArray(ResolveResult[]::new);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final QLIdeService svc = myElement.getProject().getService(QLIdeService.class);
    if (!svc.isApplicableProject(myElement.getProject())) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    return svc.getSchemaRegistry().getObjectDefinitions().stream()
        .map(typeDefinition -> buildElement(svc, typeDefinition))
        .toArray(LookupElement[]::new);
  }

  @NotNull
  private LookupElement buildElement(
      @NotNull QLIdeService svc, @NotNull ObjectTypeDefinition typeDefinition) {
    Objects.requireNonNull(svc, "svc");
    Objects.requireNonNull(typeDefinition, "typeDefinition");

    final Icon icon = getIcon(svc, typeDefinition);

    final LookupElement intermediateLookupElement =
        LookupElementBuilder.create(typeDefinition.getName())
            .withPsiElement(typeDefinition.getElement())
            .withIcon(icon);

    return PrioritizedLookupElement.withPriority(
        intermediateLookupElement, decidePriority(typeDefinition));
  }

  @NotNull
  private Icon getIcon(@NotNull QLIdeService svc, @NotNull ObjectTypeDefinition typeDefinition) {
    Objects.requireNonNull(svc, "svc");
    Objects.requireNonNull(typeDefinition, "typeDefinition");

    if (svc.isApolloFederationNode(typeDefinition)) {
      return QLIcons.INSTANCE.getApollo();
    }

    if (svc.isIntrospectionNode(typeDefinition)) {
      return QLIcons.INSTANCE.getIntrospectionFieldType();
    }

    switch (typeDefinition.getName()) {
      case "Query":
        return QLIcons.INSTANCE.getQuery();
      case "Mutation":
        return QLIcons.INSTANCE.getMutation();
      case "Subscription":
        return QLIcons.INSTANCE.getSubscription();
      default:
        return QLIcons.INSTANCE.getType();
    }
  }

  private double decidePriority(@NotNull ObjectTypeDefinition typeDefinition) {
    Objects.requireNonNull(typeDefinition, "typeDefinition");

    if (typeDefinition.getName().startsWith("__")) {
      return 0.5;
    }
    if (typeDefinition.getName().startsWith("_")) {
      return 0.0;
    }
    return 1.0;
  }
}

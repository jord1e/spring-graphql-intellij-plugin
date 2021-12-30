package nl.jrdie.idea.springql.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.jsgraphql.types.language.AbstractNode;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ArrayUtilRt;
import java.util.Objects;
import nl.jrdie.idea.springql.icons.QLIcons;
import nl.jrdie.idea.springql.svc.QLIdeService;
import nl.jrdie.idea.springql.types.SchemaMappingSummary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;

public class QLArgumentNamePolyReference extends PsiPolyVariantReferenceBase<PsiElement> {

  public QLArgumentNamePolyReference(@NotNull PsiElement psiElement) {
    super(Objects.requireNonNull(psiElement, "element"));
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final UMethod uMethod = UastContextKt.getUastParentOfType(myElement, UMethod.class);
    if (uMethod == null) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    final QLIdeService svc = myElement.getProject().getService(QLIdeService.class);
    final SchemaMappingSummary summary = svc.getSummaryForMethod(uMethod);
    if (summary == null) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    final FieldDefinition fieldDefinition =
        svc.getSchemaRegistry().getFieldDefinition(summary.getTypeName(), summary.getFieldName());
    if (fieldDefinition == null) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    return fieldDefinition.getInputValueDefinitions().stream()
        .map(
            input ->
                LookupElementBuilder.create(input.getName())
                    .withTypeText(TypeUtil.simplePrint(input.getType()))
                    .withPsiElement(input.getElement())
                    .withIcon(QLIcons.INSTANCE.getVariable()))
        .toArray(LookupElement[]::new);
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final UMethod uMethod = UastContextKt.getUastParentOfType(myElement, UMethod.class);
    if (uMethod == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    final QLIdeService svc = myElement.getProject().getService(QLIdeService.class);
    final SchemaMappingSummary summary = svc.getSummaryForMethod(uMethod);
    if (summary == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    final FieldDefinition fieldDefinition =
        svc.getSchemaRegistry().getFieldDefinition(summary.getTypeName(), summary.getFieldName());
    if (fieldDefinition == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    return fieldDefinition.getInputValueDefinitions().stream()
        .map(AbstractNode::getElement)
        .filter(Objects::nonNull)
        .map(PsiElementResolveResult::new)
        .toArray(ResolveResult[]::new);
  }
}

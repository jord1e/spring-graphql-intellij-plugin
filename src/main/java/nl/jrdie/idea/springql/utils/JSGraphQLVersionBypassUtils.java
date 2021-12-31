package nl.jrdie.idea.springql.utils;

import com.intellij.openapi.project.Project;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Psi Project error :,(
 *
 * @see JSGraphQLPlugin
 */
public class JSGraphQLVersionBypassUtils {

  private JSGraphQLVersionBypassUtils() {}

  public static boolean isApolloFederationEnabled(Project project) {
    try {
      Class<?> libTypes =
          Class.forName("com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes");
      Field federation = libTypes.getDeclaredField("FEDERATION");
      Object federationStatic = federation.get(null);
      Method isEnabled = federationStatic.getClass().getMethod("isEnabled", Project.class);
      return (boolean) isEnabled.invoke(federationStatic, project);
    } catch (Exception e) {
      return false;
    }
    //    return GraphQLLibraryTypes.FEDERATION.isEnabled(project);
  }
}

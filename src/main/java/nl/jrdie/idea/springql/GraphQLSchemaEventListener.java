package nl.jrdie.idea.springql;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface GraphQLSchemaEventListener {
  void onGraphQLSchemaChanged(@Nullable Integer schemaVersion);
}

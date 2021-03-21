package com.tambapps.maven.dependency.resolver;


import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.BaseArtifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import com.tambapps.maven.dependency.resolver.data.Scope;
import com.tambapps.maven.dependency.resolver.repository.MavenRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DependencyResolver {

  private DependencyResolver() {}

  public static List<Dependency> resolve(MavenRepository repository, String dependencyString)
      throws IOException {
    String[] fields = extractFields(dependencyString);
    return resolve(repository, fields[0], fields[1], fields[2]);
  }

  public static List<Dependency> resolve(MavenRepository repository, Artifact artifact)
      throws IOException {
    return resolve(repository, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  public static List<Dependency> resolve(MavenRepository repository, String groupId, String artifactId, String version)
      throws IOException {
    // TODO call dependencies.add() SOMEWHERE
    List<Dependency> dependencies = new ArrayList<>();
    Artifact artifact = repository.retrieveArtifact(groupId, artifactId, version);
    Set<BaseArtifact> traversedArtifacts = new HashSet<>();
    traversedArtifacts.add(artifact);
    for (Dependency dependency : artifact.getDependencies()) {
      if (dependency.isOptional() || dependency.getScope() != Scope.COMPILE) {
        continue;
      }
      resolveRec(repository, traversedArtifacts, dependencies, dependency);
    }
    return dependencies;
  }

  private static void resolveRec(MavenRepository repository,
      Set<BaseArtifact> traversedArtifacts, List<Dependency> dependencies, Dependency artifactDependency)
      throws IOException {
    if (traversedArtifacts.contains(artifactDependency)) {
      return;
    }
    Artifact artifact = repository.retrieveArtifact(artifactDependency.getGroupId(),
        artifactDependency.getArtifactId(), artifactDependency.getVersion());
    for (Dependency dependency : artifact.getDependencies()) {
      if (dependency.isOptional() || dependency.getScope() != Scope.COMPILE) {
        continue;
      }
      resolveRec(repository, traversedArtifacts, dependencies, dependency);
    }
  }

  private static String[] extractFields(String dependencyString) {
    String[] fields = dependencyString.split(":");
    if (fields.length != 3) {
      throw new IllegalArgumentException("Argument should be in pattern artifactId:groupId:version");
    }
    return fields;
  }
}

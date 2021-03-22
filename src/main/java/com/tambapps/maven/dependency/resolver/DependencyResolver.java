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

  public static List<BaseArtifact> resolve(MavenRepository repository, String dependencyString)
      throws IOException {
    String[] fields = extractFields(dependencyString);
    return resolve(repository, fields[0], fields[1], fields[2]);
  }

  public static List<BaseArtifact> resolve(MavenRepository repository, Artifact artifact)
      throws IOException {
    return resolve(repository, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  public static List<BaseArtifact> resolve(MavenRepository repository, String groupId, String artifactId, String version)
      throws IOException {
    List<BaseArtifact> dependencies = new ArrayList<>();
    Artifact artifact = repository.retrieveArtifact(groupId, artifactId, version);
    resolveRec(repository, new HashSet<>(), dependencies, artifact);
    return dependencies;
  }

  private static void resolveRec(MavenRepository repository,
      Set<BaseArtifact> traversedArtifacts, List<BaseArtifact> dependencies, BaseArtifact artifactDependency)
      throws IOException {
    if (traversedArtifacts.contains(artifactDependency)) {
      return;
    }
    Artifact artifact = repository.retrieveArtifact(artifactDependency.getGroupId(),
        artifactDependency.getArtifactId(), artifactDependency.getVersion());
    BaseArtifact baseArtifact = artifact.toBase();
    dependencies.add(baseArtifact);
    traversedArtifacts.add(baseArtifact);
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

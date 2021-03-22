package com.tambapps.maven.dependency.resolver;


import com.tambapps.maven.dependency.resolver.data.DependencyResolvingResult;
import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import com.tambapps.maven.dependency.resolver.data.Scope;
import com.tambapps.maven.dependency.resolver.repository.MavenRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DependencyResolver {

  private DependencyResolver() {}

  public static DependencyResolvingResult resolve(MavenRepository repository, String dependencyString)
      throws IOException {
    String[] fields = extractFields(dependencyString);
    return resolve(repository, fields[0], fields[1], fields[2]);
  }

  public static DependencyResolvingResult resolve(MavenRepository repository, PomArtifact pomArtifact)
      throws IOException {
    return resolve(repository, pomArtifact.getGroupId(), pomArtifact.getArtifactId(), pomArtifact.getVersion());
  }

  public static DependencyResolvingResult resolve(MavenRepository repository, String groupId, String artifactId, String version)
      throws IOException {
    return resolve(repository, Collections.singletonList(new Artifact(groupId, artifactId, version)));
  }

  public static DependencyResolvingResult resolve(MavenRepository repository, List<Artifact> artifacts)
      throws IOException {
    List<Artifact> dependencies = new ArrayList<>();
    Set<Artifact> visitedArtifacts = new HashSet<>();
    for (Artifact artifact : artifacts) {
      resolveRec(repository, visitedArtifacts, dependencies, artifact.toBase());
    }
    Map<String, List<Artifact>> artifactVersionsMap = new HashMap<>();
    for (Artifact artifact : dependencies) {
      artifactVersionsMap.computeIfAbsent(String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId()), k -> new ArrayList<>())
          .add(artifact);
    }
    return new DependencyResolvingResult(dependencies, artifactVersionsMap);
  }

  private static void resolveRec(MavenRepository repository,
      Set<Artifact> visitedArtifacts, List<Artifact> dependencies, Artifact artifactDependency)
      throws IOException {
    if (visitedArtifacts.contains(artifactDependency)) {
      return;
    }
    PomArtifact pomArtifact = repository.retrieveArtifact(artifactDependency.getGroupId(),
        artifactDependency.getArtifactId(), artifactDependency.getVersion());
    Artifact artifact = pomArtifact.toBase();
    dependencies.add(artifact);
    visitedArtifacts.add(artifact);
    // TODO also fetch parent dependencies if any
    for (Dependency dependency : pomArtifact.getDependencies()) {
      if (dependency.isOptional() || dependency.getScope() != Scope.COMPILE) {
        continue;
      }
      resolveRec(repository, visitedArtifacts, dependencies, dependency);
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

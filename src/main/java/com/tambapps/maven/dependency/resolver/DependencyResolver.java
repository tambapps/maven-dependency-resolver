package com.tambapps.maven.dependency.resolver;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import com.tambapps.maven.dependency.resolver.data.DependencyResolvingResult;
import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.data.Scope;
import com.tambapps.maven.dependency.resolver.repository.MavenRepository;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class allowing to fetch an artifact along with its transitive dependencies.
 * This class is definitely NOT thread-safe
 */
public class DependencyResolver {

  private final MavenRepository repository;
  @Getter
  private final List<Artifact> fetchedArtifacts = new ArrayList<>();

  public DependencyResolver(MavenRepository repository) {
    this.repository = repository;
  }

  public void resolve(Artifact artifact) throws IOException {
    if (artifact instanceof PomArtifact) {
      resolve((PomArtifact) artifact);
    } else {
      resolve(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    }
  }

  public void resolve(String groupId, String artifactId, String version) throws IOException {
    resolve(repository.retrieveArtifact(groupId, artifactId, version));
  }

  public void resolve(PomArtifact pomArtifact) throws IOException {
    fetchedArtifacts.add(pomArtifact);

    for (Dependency dependency : pomArtifact.getDependencies()) {
      // here we gooooooooo
      List<Dependency> dependencyPath = new ArrayList<>();
      if (shouldSkip(dependency, dependencyPath)) {
        continue;
      }
      resolveRec(dependency, dependencyPath);
    }
  }

  public DependencyResolvingResult getResults() {
    // map groupId:artifactId -> versions
    Map<String, List<Artifact>> artifactVersionsMap = new HashMap<>();

    for (Artifact artifact : fetchedArtifacts) {
      artifactVersionsMap.computeIfAbsent(artifact.getGroupId() + ":" + artifact.getArtifactId(), k -> new ArrayList<>())
          .add(artifact);
    }
    return new DependencyResolvingResult(fetchedArtifacts, artifactVersionsMap);
  }

  public void reset() {
    fetchedArtifacts.clear();
  }

  /**
   * Resolve recursively the dependencies
   *
   * @param dependency     the dependency to resolve
   * @param dependencyPath the dependency path, allowing to track in the dependency graph the parent dependencies of the one we're fetching. (Used to check exclusion)
   * @throws IOException in case of I/O error
   */
  private void resolveRec(Dependency dependency, List<Dependency> dependencyPath) throws IOException {
    PomArtifact pomArtifact = repository.retrieveArtifact(dependency);
    fetchedArtifacts.add(pomArtifact);

    for (Dependency artifactDependency : pomArtifact.getDependencies()) {
      if (shouldSkip(artifactDependency, dependencyPath)) {
        continue;
      }
      List<Dependency> childDependencyBranch = new ArrayList<>(dependencyPath);
      childDependencyBranch.add(dependency);
      resolveRec(artifactDependency, childDependencyBranch);
    }
  }

  private boolean shouldSkip(Dependency artifactDependency, List<Dependency> dependencyPath) {
    return artifactDependency.isOptional() ||
        artifactDependency.getScope() != Scope.COMPILE ||
        fetchedArtifacts.stream().anyMatch(artifactDependency::matches) ||
        isExcluded(artifactDependency, dependencyPath);
  }

  private boolean isExcluded(Dependency dependency, List<Dependency> dependencyBranch) {
    return dependencyBranch.stream()
        .anyMatch(db -> db.isAnyArtifact() || db.matches(dependency));
  }

}

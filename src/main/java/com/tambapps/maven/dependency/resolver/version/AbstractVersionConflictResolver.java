package com.tambapps.maven.dependency.resolver.version;

import com.tambapps.maven.dependency.resolver.data.Artifact;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractVersionConflictResolver implements VersionConflictResolver {
  @Override
  public List<Artifact> resolveConflicts(Map<String, List<Artifact>> conflicts) {
    return conflicts.values().stream().map(this::selectArtifact).collect(Collectors.toList());
  }

  protected abstract Artifact selectArtifact(List<Artifact> artifacts);
}

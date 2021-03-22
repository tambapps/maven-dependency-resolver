package com.tambapps.maven.dependency.resolver.version;

import com.tambapps.maven.dependency.resolver.data.Artifact;

import java.util.List;

/**
 * Use the first
 */
public class FirstVersionFoundConflictResolver extends AbstractVersionConflictResolver {

  @Override
  protected Artifact selectArtifact(List<Artifact> artifacts) {
    return artifacts.get(0);
  }
}

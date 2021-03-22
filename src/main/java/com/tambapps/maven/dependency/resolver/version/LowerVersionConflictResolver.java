package com.tambapps.maven.dependency.resolver.version;

import com.tambapps.maven.dependency.resolver.data.Artifact;

import java.util.Comparator;
import java.util.List;

/**
 * Use the lower version
 */
public class LowerVersionConflictResolver extends AbstractVersionConflictResolver {

  @Override
  protected Artifact selectArtifact(List<Artifact> artifacts) {
    return artifacts.stream().min(Comparator.comparing(Artifact::getVersion)).get();
  }
}

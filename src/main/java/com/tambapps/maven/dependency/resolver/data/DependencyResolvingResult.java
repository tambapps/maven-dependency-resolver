package com.tambapps.maven.dependency.resolver.data;

import com.tambapps.maven.dependency.resolver.version.VersionConflictResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependencyResolvingResult {
  List<Artifact> fetchedArtifacts;
  // map groupId:artifactId -> versions
  Map<String, List<Artifact>> artifactVersionsMap;

  public List<Artifact> getArtifacts(VersionConflictResolver versionConflictResolver) {
    return versionConflictResolver.resolveConflicts(artifactVersionsMap);
  }
}

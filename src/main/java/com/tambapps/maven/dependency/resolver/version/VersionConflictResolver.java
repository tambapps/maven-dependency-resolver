package com.tambapps.maven.dependency.resolver.version;

import com.tambapps.maven.dependency.resolver.data.Artifact;

import java.util.List;
import java.util.Map;

/**
 * Resolve artifacts versions conflicts if any
 */
public interface VersionConflictResolver {

  List<Artifact> resolveConflicts(Map<String, List<Artifact>> conflicts);

}

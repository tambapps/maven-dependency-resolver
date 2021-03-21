package com.tambapps.maven.dependency.resolver.repository;

import java.io.File;

public class LocalRepository extends AbstractRepository {

  // may be useless
  private final File root;
  private final File repoRoot;

  public LocalRepository(File root) {
    this.root = root;
    repoRoot = new File(root, "repository");
  }

  @Override
  public boolean exists(String artifactId, String groupId, String version) {
    return new File(repoRoot, getJarKey(artifactId, groupId, version)).exists();
  }
}

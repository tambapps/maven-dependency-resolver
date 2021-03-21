package com.tambapps.maven.dependency.resolver.repository;

import java.io.File;

public class LocalMavenRepository extends AbstractMavenRepository {

  // may be useless
  private final File root;
  private final File repoRoot;

  public LocalMavenRepository(File root) {
    this.root = root;
    repoRoot = new File(root, "repository");
  }

  @Override
  public boolean exists(String groupId, String artifactId, String version) {
    return new File(repoRoot, getJarKey(groupId, artifactId, version)).exists();
  }
}

package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

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

  @Override
  public InputStream retrieveArtifactJar(String groupId, String artifactId, String version)
      throws IOException {
    File file = new File(repoRoot, getJarKey(groupId, artifactId, version));
    if (!file.exists()) {
      throw new ArtifactNotFoundException();
    }
    return new FileInputStream(file);
  }

  /*
  @Override
  protected InputStream jarStream(String groupId, String artifactId, String version) throws IOException {
    return new FileInputStream(new File(repoRoot, getJarKey(groupId, artifactId, version)));
  }
 */
}

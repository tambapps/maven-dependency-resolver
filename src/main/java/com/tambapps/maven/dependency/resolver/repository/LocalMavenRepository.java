package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class LocalMavenRepository extends AbstractMavenRepository {

  // may be useless
  private final File root;
  private final File repoRoot;

  public LocalMavenRepository(File root) {
    this.root = root;
    repoRoot = new File(root, "repository");
  }

  // overridden to remove throws IOException
  @Override
  public boolean exists(String dependencyString) {
    String[] fields = extractFields(dependencyString);
    return exists(fields[0], fields[1], fields[2]);
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

  @Override
  public InputStream retrieveArtifactPom(String groupId, String artifactId, String version)
      throws IOException {
    File file = new File(repoRoot, getPomKey(groupId, artifactId, version));
    if (!file.exists()) {
      throw new ArtifactNotFoundException();
    }
    return new FileInputStream(file);
  }

  @Override
  public Artifact retrieveArtifact(String groupId, String artifactId, String version)
      throws IOException {
    File file = new File(repoRoot, getPomKey(groupId, artifactId, version));
    if (!file.exists()) {
      throw new ArtifactNotFoundException();
    }
    try (InputStream inputStream = new FileInputStream(file)) {
      return toArtifact(inputStream);
    }
  }

  public void saveArtifactJar(Artifact artifact, InputStream inputStream) throws IOException {
    saveArtifactJar(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), inputStream);
  }

  public void saveArtifactJar(String groupId, String artifactId, String version, InputStream inputStream) throws IOException {
    File file = new File(repoRoot, getJarKey(groupId, artifactId, version));
    Files.copy(inputStream, file.toPath());
  }
  public void saveArtifactPom(Artifact artifact, InputStream inputStream) throws IOException {
    saveArtifactPom(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), inputStream);
  }
  public void saveArtifactPom(String groupId, String artifactId, String version, InputStream inputStream) throws IOException {
    File file = new File(repoRoot, getPomKey(groupId, artifactId, version));
    Files.copy(inputStream, file.toPath());
  }
}

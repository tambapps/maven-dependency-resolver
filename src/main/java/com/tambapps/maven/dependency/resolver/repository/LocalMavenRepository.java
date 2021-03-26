package com.tambapps.maven.dependency.resolver.repository;

import static com.tambapps.maven.dependency.resolver.data.Artifact.extractFields;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalMavenRepository extends AbstractMavenRepository {

  // may be useless
  protected final File root;
  protected final File repoRoot;

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
  public PomArtifact retrieveArtifact(String groupId, String artifactId, String version)
      throws IOException {
    File file = new File(repoRoot, getPomKey(groupId, artifactId, version));
    if (!file.exists()) {
      throw new ArtifactNotFoundException();
    }
    try (InputStream inputStream = new FileInputStream(file)) {
      return toArtifact(inputStream);
    }
  }

  public List<Artifact> getAllArtifacts() throws IOException {
    return Files.walk(repoRoot.toPath()).filter(this::repoJarFile)
        .map(this::toArtifact)
        .collect(Collectors.toList());
  }

  // groupId -> artifactId -> List<Artifact>
  public Map<String, Map<String, List<String>>> listArtifacts() throws IOException {
    Map<String, Map<String, List<String>>> map = new HashMap<>();
    Files.walk(repoRoot.toPath()).filter(this::repoJarFile)
        .map(this::toArtifact)
        .forEach(a -> map.computeIfAbsent(a.getGroupId(), k -> new HashMap<>())
            .computeIfAbsent(a.getArtifactId(), k -> new ArrayList<>())
            .add(a.getVersion()));
    return map;
  }

  public List<File> getJarFiles(List<Artifact> artifacts) {
    return artifacts.stream()
        .map(a -> new File(repoRoot, getJarKey(a.getGroupId(), a.getArtifactId(), a.getVersion())))
        .filter(File::exists)
        .collect(Collectors.toList());
  }

  public File getJarFile(Artifact a) {
    File jarFile = new File(repoRoot, getJarKey(a.getGroupId(), a.getArtifactId(), a.getVersion()));
    if (!jarFile.exists()) {
      throw new ArtifactNotFoundException();
    }
    return jarFile;
  }

  private boolean repoJarFile(Path path) {
    String pathString = path.toAbsolutePath().toString();
    if (!Files.isRegularFile(path) || !pathString.endsWith(".jar")) {
      return false;
    }
    String[] fields = pathString.split("/");
    if (fields.length < 2) {
      return false;
    }
    // now stuff to match library jar, not javadoc or source.
    // library jar should ends with ${version}.jar
    String version = fields[fields.length - 2];
    return pathString.endsWith(version + ".jar");
  }

  private Artifact toArtifact(Path path) {
    String pathString = path.toAbsolutePath().toString();
    String relativePath = pathString.substring(repoRoot.getAbsolutePath().length() + 1);
    String[] fields = relativePath.split("/");
    String version = fields[fields.length - 2];
    String artifactId = fields[fields.length - 1].substring(0, fields[fields.length - 1].length() - version.length() - 5); // minus 5 for '-' and '.jar'

    int artifactIdIndex = fields.length - 1;
    while (!fields[artifactIdIndex].equals(artifactId)) artifactIdIndex--;
    String groupId = String.join(".", Arrays.copyOfRange(fields, 0, artifactIdIndex));
    return new Artifact(groupId, artifactId, version);
  }

  public void saveArtifactJar(PomArtifact pomArtifact, InputStream inputStream) throws IOException {
    saveArtifactJar(pomArtifact.getGroupId(), pomArtifact.getArtifactId(), pomArtifact.getVersion(), inputStream);
  }

  public void saveArtifactJar(String groupId, String artifactId, String version, InputStream inputStream) throws IOException {
    File file = new File(repoRoot, getJarKey(groupId, artifactId, version));
    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
      throw new IOException("Couldn't create directory " + file.getParent());
    }
    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }
  public void saveArtifactPom(PomArtifact pomArtifact, InputStream inputStream) throws IOException {
    saveArtifactPom(pomArtifact.getGroupId(), pomArtifact.getArtifactId(), pomArtifact.getVersion(), inputStream);
  }
  public void saveArtifactPom(String groupId, String artifactId, String version, InputStream inputStream) throws IOException {
    File file = new File(repoRoot, getPomKey(groupId, artifactId, version));
    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
      throw new IOException("Couldn't create directory " + file.getParent());
    }
    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }
}

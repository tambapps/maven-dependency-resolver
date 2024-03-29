package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.exception.ArtifactNotFoundException;
import com.tambapps.maven.dependency.resolver.storage.LocalRepositoryStorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalMavenRepository extends MavenRepository {

  protected final File root;
  protected final File repoRoot;

  public LocalMavenRepository() {
    this(new File(System.getProperty("user.home"), ".m2"));
  }

  public LocalMavenRepository(File root) {
    super(new LocalRepositoryStorage(new File(root, "repository")));
    this.root = root;
    repoRoot = new File(root, "repository");
  }

  public List<Artifact> getAllArtifacts() throws IOException {
    if (!repoRoot.exists()) {
      return Collections.emptyList();
    }
    return Files.walk(repoRoot.toPath()).filter(this::repoPomFile)
        .map(this::toArtifact)
        .collect(Collectors.toList());
  }

  // groupId -> artifactId -> List<Artifact>
  public Map<String, Map<String, List<String>>> listArtifacts() throws IOException {
    if (!repoRoot.exists()) {
      return Collections.emptyMap();
    }
    Map<String, Map<String, List<String>>> map = new HashMap<>();
    Files.walk(repoRoot.toPath()).filter(this::repoPomFile)
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
    if (!jarFile.getParentFile().exists()) {
      throw new ArtifactNotFoundException(a.getGroupId(), a.getArtifactId(), a.getVersion());
    } else if (!jarFile.exists()) {
      // it's a pom artifact, with no jar
      return null;
    }
    return jarFile;
  }

  public File getPomFile(Artifact a) {
    File pomFile = new File(repoRoot, getPomKey(a.getGroupId(), a.getArtifactId(), a.getVersion()));
    if (!pomFile.exists()) {
      throw new ArtifactNotFoundException(a.getGroupId(), a.getArtifactId(), a.getVersion());
    }
    return pomFile;
  }

  public File getSettingsFile() {
    return new File(root, "settings.xml");
  }

  public File getSecuritySettingsFile() {
    return new File(root, "settings-security.xml");
  }

  private boolean repoPomFile(Path path) {
    String pathString = path.toAbsolutePath().toString();
    if (!Files.isRegularFile(path) || !pathString.endsWith(POM_SUFFIX)) {
      return false;
    }
    String[] fields = pathString.split("/");
    if (fields.length < 2) {
      return false;
    }
    // now stuff to match library jar, not javadoc or source.
    // library jar should ends with ${version}.pom
    String version = fields[fields.length - 2];
    return pathString.endsWith(version + POM_SUFFIX);
  }

  private Artifact toArtifact(Path path) {
    String pathString = path.toAbsolutePath().toString();
    String relativePath = pathString.substring(repoRoot.getAbsolutePath().length() + 1);
    String[] fields = relativePath.split("/");
    String version = fields[fields.length - 2];
    String artifactId = fields[fields.length - 1].substring(0, fields[fields.length - 1].length() - version.length() - 5); // minus 5 for '-' and '.pom'

    int artifactIdIndex = fields.length - 1;
    while (!fields[artifactIdIndex].equals(artifactId)) artifactIdIndex--;
    String groupId = String.join(".", Arrays.copyOfRange(fields, 0, artifactIdIndex));
    return new Artifact(groupId, artifactId, version);
  }

  public void saveArtifactJar(Artifact artifact, InputStream inputStream) throws IOException {
    saveArtifactJar(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), inputStream);
  }

  public void saveArtifactJar(String groupId, String artifactId, String version, InputStream inputStream) throws IOException {
    File file = new File(repoRoot, getJarKey(groupId, artifactId, version));
    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
      throw new IOException("Couldn't create directory " + file.getParent());
    }
    doSaveArtifactJar(file, inputStream);
  }

  // overridable
  protected void doSaveArtifactJar(File file, InputStream inputStream) throws IOException {
    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }

  public void saveArtifactPom(Artifact artifact, InputStream inputStream) throws IOException {
    saveArtifactPom(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), inputStream);
  }

  public void saveArtifactPom(String groupId, String artifactId, String version, InputStream inputStream) throws IOException {
    File file = new File(repoRoot, getPomKey(groupId, artifactId, version));
    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
      throw new IOException("Couldn't create directory " + file.getParent());
    }
    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }

  public void deleteAllArtifacts() {
    File[] files = repoRoot.listFiles();
    if (files == null) return;
    for (File file : files) {
      if (file.isDirectory()) deleteDirectory(file);
      else file.delete();
    }
  }

  public boolean deleteArtifact(Artifact artifact) {
    return deleteArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  public boolean deleteArtifact(String groupId, String artifactId, String version) {
    File file = new File(repoRoot, getPomKey(groupId, artifactId, version));
    if (!file.exists()) return false;
    if (file.getParentFile() == null) return false;
    return deleteDirectory(file.getParentFile());
  }

  private boolean deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        deleteDirectory(file);
      }
    }
    return directoryToBeDeleted.delete();
  }
}

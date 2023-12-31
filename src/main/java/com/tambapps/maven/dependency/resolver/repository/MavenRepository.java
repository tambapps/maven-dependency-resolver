package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.exception.ArtifactNotFoundException;
import com.tambapps.maven.dependency.resolver.exception.ResourceNotFound;
import com.tambapps.maven.dependency.resolver.io.PomParser;
import com.tambapps.maven.dependency.resolver.storage.RepositoryStorage;

import java.io.IOException;
import java.io.InputStream;

import static com.tambapps.maven.dependency.resolver.data.Artifact.extractFields;


public class MavenRepository {

  protected static final String JAR_SUFFIX = ".jar";
  protected static final String POM_SUFFIX = ".pom";

  private final PomParser pomParser = new PomParser(this);
  protected final RepositoryStorage repositoryStorage;

  public MavenRepository(RepositoryStorage repositoryStorage) {
    this.repositoryStorage = repositoryStorage;
  }

  public boolean exists(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return exists(fields[0], fields[1], fields[2]);
  }

  public boolean exists(String groupId, String artifactId, String version) throws IOException {
    return repositoryStorage.exists(getPomKey(groupId, artifactId, version));
  }

  public PomArtifact retrieveArtifact(Artifact artifact) throws IOException {
    return retrieveArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  public PomArtifact retrieveArtifact(String groupId, String artifactId, String version) throws IOException {
    try (InputStream inputStream = retrieveArtifactPom(groupId, artifactId, version)) {
      return toArtifact(inputStream);
    }
  }

  public InputStream retrieveArtifactJar(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifactJar(fields[0], fields[1], fields[2]);
  }

  public InputStream retrieveArtifactJar(String groupId, String artifactId, String version) throws IOException {
    try {
      return repositoryStorage.get(getJarKey(groupId, artifactId, version));
    } catch (ResourceNotFound e) {
      throw new ArtifactNotFoundException(groupId, artifactId, version);
    }
  }

  public InputStream retrieveArtifactPom(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifactPom(fields[0], fields[1], fields[2]);
  }

  public InputStream retrieveArtifactPom(String groupId, String artifactId, String version) throws IOException {
    try {
      return repositoryStorage.get(getPomKey(groupId, artifactId, version));
    } catch (ResourceNotFound e) {
      throw new ArtifactNotFoundException(groupId, artifactId, version);
    }
  }

  protected PomArtifact toArtifact(InputStream inputStream) throws IOException {
    return pomParser.parse(inputStream);
  }

  protected String getKey(String groupId, String artifactId, String version) {
    return groupId.replaceAll("\\.", "/") + "/" +
        artifactId + "/" + version + "/" + artifactId + "-" + version;
  }

  protected String getPomKey(String groupId, String artifactId, String version) {
    return getKey(groupId, artifactId, version) + POM_SUFFIX;
  }

  protected String getJarKey(String groupId, String artifactId, String version) {
    return getKey(groupId, artifactId, version) + JAR_SUFFIX;
  }

}

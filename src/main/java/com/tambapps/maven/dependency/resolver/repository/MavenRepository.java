package com.tambapps.maven.dependency.resolver.repository;

import static com.tambapps.maven.dependency.resolver.data.Artifact.extractFields;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;

import java.io.IOException;
import java.io.InputStream;

public interface MavenRepository {

  default boolean exists(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return exists(fields[0], fields[1], fields[2]);
  }

  boolean exists(String groupId, String artifactId, String version) throws IOException;

  default InputStream retrieveArtifactJar(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifactJar(fields[0], fields[1], fields[2]);
  }

  InputStream retrieveArtifactJar(String groupId, String artifactId, String version) throws IOException;

  default InputStream retrieveArtifactPom(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifactPom(fields[0], fields[1], fields[2]);
  }

  InputStream retrieveArtifactPom(String groupId, String artifactId, String version) throws IOException;

  default PomArtifact retrieveArtifact(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifact(fields[0], fields[1], fields[2]);
  }

  PomArtifact retrieveArtifact(String groupId, String artifactId, String version) throws IOException;

}

package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;

import java.io.IOException;
import java.io.InputStream;

public interface MavenRepository {

  boolean exists(String dependencyString) throws IOException;

  boolean exists(String groupId, String artifactId, String version) throws IOException;

  InputStream retrieveArtifactJar(String dependencyString) throws IOException;

  InputStream retrieveArtifactJar(String groupId, String artifactId, String version) throws IOException;

  InputStream retrieveArtifactPom(String dependencyString) throws IOException;

  InputStream retrieveArtifactPom(String groupId, String artifactId, String version) throws IOException;

  PomArtifact retrieveArtifact(String dependencyString) throws IOException;

  PomArtifact retrieveArtifact(String groupId, String artifactId, String version) throws IOException;

}

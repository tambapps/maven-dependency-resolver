package com.tambapps.maven.dependency.resolver.repository;

import java.io.IOException;
import java.io.InputStream;

public interface MavenRepository {

  boolean exists(String dependencyString) throws IOException;

  boolean exists(String groupId, String artifactId, String version) throws IOException;

  InputStream retrieveArtifactJar(String dependencyString) throws IOException;

  InputStream retrieveArtifactJar(String groupId, String artifactId, String version) throws IOException;

}

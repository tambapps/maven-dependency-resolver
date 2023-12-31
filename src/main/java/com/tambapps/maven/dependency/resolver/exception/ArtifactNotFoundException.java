package com.tambapps.maven.dependency.resolver.exception;

public class ArtifactNotFoundException extends RuntimeException {

  public ArtifactNotFoundException(String groupId, String artifactId, String versionId) {
    super(String.format("Artifact %s:%s:%s was not found", groupId, artifactId, versionId));
  }
}

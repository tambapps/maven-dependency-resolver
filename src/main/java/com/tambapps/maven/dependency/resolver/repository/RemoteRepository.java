package com.tambapps.maven.dependency.resolver.repository;

public class RemoteRepository extends AbstractRepository {

  @Override public boolean exists(String artifactId, String groupId, String version) {
    return false;
  }
}

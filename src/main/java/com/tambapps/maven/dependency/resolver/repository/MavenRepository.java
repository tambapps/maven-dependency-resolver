package com.tambapps.maven.dependency.resolver.repository;

import java.io.IOException;

public interface MavenRepository {

  default boolean exists(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return exists(fields[0], fields[1], fields[2]);
  }

  boolean exists(String groupId, String artifactId, String version) throws IOException;


  default String[] extractFields(String dependencyString) {
    String[] fields = dependencyString.split(":");
    if (fields.length != 3) {
      throw new IllegalArgumentException("Argument should be in pattern artifactId:groupId:version");
    }
    return fields;
  }
}

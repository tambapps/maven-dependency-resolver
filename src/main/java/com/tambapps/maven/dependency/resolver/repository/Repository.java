package com.tambapps.maven.dependency.resolver.repository;

public interface Repository {

  default boolean exists(String dependencyString) {
    String[] fields = extractFields(dependencyString);
    return exists(fields[0], fields[1], fields[2]);
  }

  boolean exists(String artifactId, String groupId, String version);


  default String[] extractFields(String dependencyString) {
    String[] fields = dependencyString.split(":");
    if (fields.length != 3) {
      throw new IllegalArgumentException("Argument should be in pattern artifactId:groupId:version");
    }
    return fields;
  }
}

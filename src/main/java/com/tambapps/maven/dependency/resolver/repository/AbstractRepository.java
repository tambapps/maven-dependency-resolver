package com.tambapps.maven.dependency.resolver.repository;

public abstract class AbstractRepository implements Repository {

  protected String getKey(String groupId, String artifactId, String version) {
    return groupId.replaceAll("\\.", "/") + "/" +
        artifactId.replaceAll("\\.", "/") + "/" + version;
  }

  protected String getPomKey(String groupId, String artifactId, String version) {
    return groupId.replaceAll("\\.", "/") + "/" +
        artifactId.replaceAll("\\.", "/") + "/" + version + "/" +
        artifactId + "-" + version + ".pom";
  }

  protected String getJarKey(String groupId, String artifactId, String version) {
    return groupId.replaceAll("\\.", "/") + "/" +
        artifactId.replaceAll("\\.", "/") + "/" + version + "/" +
        artifactId + "-" + version + ".pom";
  }
}

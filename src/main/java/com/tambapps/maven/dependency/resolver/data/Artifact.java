package com.tambapps.maven.dependency.resolver.data;

import lombok.Data;

import java.util.List;

@Data
public class Artifact {
  String groupId;
  String artifactId;
  String version;

  // TODO parent
  // TODO dependency managment
  List<Dependency> dependencies;

  @Override
  public String toString() {
    return String.join(":", groupId, artifactId, version);
  }
}

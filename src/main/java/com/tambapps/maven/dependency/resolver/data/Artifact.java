package com.tambapps.maven.dependency.resolver.data;

import lombok.Data;

import java.util.List;

@Data
public class Artifact {
  Artifact parent;
  String groupId;
  String artifactId;
  String version;

  // TODO parent
  // TODO dependency managment
  List<Dependency> dependencies;
  List<Dependency> dependencyManagement;

  public String getGroupId() {
    if (groupId == null) {
      return parent != null ? parent.getGroupId() : null;
    }
    return groupId;
  }

  @Override
  public String toString() {
    return String.join(":", groupId, artifactId, version);
  }
}

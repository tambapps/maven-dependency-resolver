package com.tambapps.maven.dependency.resolver.data;

import lombok.Data;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  public void setParent(Artifact parent) {
    this.parent = parent;
    // look for dependency version if some is not specified
    List<Dependency> depsWithoutVersion = dependencies.stream().filter(d -> d.getVersion() == null)
        .collect(Collectors.toList());
    Artifact parentArtifact = parent;
    while (depsWithoutVersion.size() > 0 && parentArtifact != null) {
      Iterator<Dependency> iterator = depsWithoutVersion.iterator();
      while (iterator.hasNext()) {
        Dependency dependency = iterator.next();
        Optional<Dependency> optMatchedDependencyManagement =
            parentArtifact.getDependencyManagement().stream().filter(dependency::matches).findFirst();
        optMatchedDependencyManagement.ifPresent(value -> dependency.setVersion(value.getVersion()));
        iterator.remove();
      }
      parentArtifact = parentArtifact.getParent();
    }
  }
}

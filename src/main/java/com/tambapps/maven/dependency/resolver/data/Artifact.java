package com.tambapps.maven.dependency.resolver.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
public class Artifact extends BaseArtifact {
  Artifact parent;

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

  @Override
  public boolean equals(Object o) {
   return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}

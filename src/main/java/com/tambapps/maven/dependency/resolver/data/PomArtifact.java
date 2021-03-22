package com.tambapps.maven.dependency.resolver.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
public class PomArtifact extends Artifact {
  PomArtifact parent;

  List<Dependency> dependencies;
  List<Dependency> dependencyManagement;
  Map<String, String> properties;

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

  public void setParent(PomArtifact parent) {
    this.parent = parent;
    // look for dependency version if some is not specified
    List<Dependency> depsWithoutVersion = dependencies.stream().filter(d -> d.getVersion() == null)
        .collect(Collectors.toList());
    PomArtifact parentPomArtifact = parent;
    while (depsWithoutVersion.size() > 0 && parentPomArtifact != null) {
      Iterator<Dependency> iterator = depsWithoutVersion.iterator();
      while (iterator.hasNext()) {
        Dependency dependency = iterator.next();
        Optional<Dependency> optMatchedDependencyManagement =
            parentPomArtifact.getDependencyManagement().stream().filter(dependency::matches).findFirst();
        optMatchedDependencyManagement.ifPresent(value -> dependency.setVersion(value.getVersion()));
        iterator.remove();
      }
      parentPomArtifact = parentPomArtifact.getParent();
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

  public String getProperty(String propertyName) {
    String propertyValue = properties.get(propertyName);
    if (propertyValue != null) {
      return propertyValue;
    } else if (parent != null) {
      return parent.getProperty(propertyName);
    } else {
      return null;
    }
  }
}

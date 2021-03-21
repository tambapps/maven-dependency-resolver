package com.tambapps.maven.dependency.resolver.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Dependency extends BaseArtifact {

  Scope scope;
  boolean optional;

  public boolean matches(Dependency dependency) {
    return getGroupId().equals(dependency.getGroupId()) &&
        getArtifactId().equals(dependency.getArtifactId()) &&
        getVersion().equals(dependency.getVersion());
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

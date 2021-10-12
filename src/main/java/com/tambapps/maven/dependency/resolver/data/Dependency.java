package com.tambapps.maven.dependency.resolver.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Dependency extends Artifact {

  Scope scope;
  boolean optional;
  // artifact versions are null
  // TODO take them into account when fetching dependencies
  List<Artifact> exclusions;

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}

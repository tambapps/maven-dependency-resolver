package com.tambapps.maven.dependency.resolver.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artifact {
  String groupId;
  String artifactId;
  String version;

  public Artifact toBase() {
    return new Artifact(groupId, artifactId, version);
  }
}

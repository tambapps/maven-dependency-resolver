package com.tambapps.maven.dependency.resolver.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseArtifact {
  String groupId;
  String artifactId;
  String version;

  public BaseArtifact toBase() {
    return new BaseArtifact(groupId, artifactId, version);
  }
}

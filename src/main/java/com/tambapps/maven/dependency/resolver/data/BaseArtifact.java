package com.tambapps.maven.dependency.resolver.data;

import lombok.Data;

@Data
public class BaseArtifact {
  String groupId;
  String artifactId;
  String version;
}

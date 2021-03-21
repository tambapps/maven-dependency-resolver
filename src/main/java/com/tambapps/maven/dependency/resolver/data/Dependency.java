package com.tambapps.maven.dependency.resolver.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Dependency {
  String groupId;
  String artifactId;
  String version;

  Scope scope;
  boolean optional;
}

package com.tambapps.maven.dependency.resolver.data;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class DependencyResolvingResult {
  List<Artifact> artifacts;
  // map groupId:artifactId -> versions
  Map<String, List<Artifact>> artifactVersionsMap;
}

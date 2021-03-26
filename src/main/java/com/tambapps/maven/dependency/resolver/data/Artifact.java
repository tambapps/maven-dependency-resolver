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

  public static String[] extractFields(String dependencyString) {
    String[] fields = dependencyString.split(":");
    if (fields.length != 3) {
      throw new IllegalArgumentException("Argument should be in pattern artifactId:groupId:version");
    }
    return fields;
  }

  public static Artifact from(String artifactString) {
    String[] fields = extractFields(artifactString);
    return new Artifact(fields[0], fields[1], fields[2]);
  }
}

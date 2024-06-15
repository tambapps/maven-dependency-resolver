package com.tambapps.maven.dependency.resolver.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artifact {

  private static final String WILDCARD = "*";

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

  public String toArtifactString() {
    return String.format("%s:%s:%s", groupId, artifactId, version);
  }

  public static Artifact from(String artifactString) {
    String[] fields = extractFields(artifactString);
    return new Artifact(fields[0], fields[1], fields[2]);
  }

  /**
   * Returns whether this artifact matches the provided one. Two artifact matches if they have the same groupId and artifactId
   * (we don't care about the version).
   * @param artifact the other artifact
   * @return whether this artifact matches the provided one
   */
  public boolean matches(Artifact artifact) {
    return matches(getGroupId(), artifact.getGroupId()) && matches(getArtifactId(), artifact.getArtifactId());
  }

  private static boolean matches(String a, String b) {
    return a.equals(b) || WILDCARD.equals(a) || WILDCARD.equals(b);
  }
  /**
   * Return whether this artifact is any artifact (groupId = artifactId = "*")
   * Useful for dependency exclusion
   * @return whether this artifact is any artifact
   */
  public boolean isAnyArtifact() {
    return WILDCARD.equals(groupId) && WILDCARD.equals(artifactId);
  }
}

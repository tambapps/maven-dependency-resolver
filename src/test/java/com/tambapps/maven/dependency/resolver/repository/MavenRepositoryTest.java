package com.tambapps.maven.dependency.resolver.repository;


import static org.junit.Assert.assertEquals;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.data.Scope;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

public class MavenRepositoryTest {

  @Test
  public void testParsePom() throws IOException {
    // don't care about the root, we just want to test fetching
    LocalMavenRepository repository = new LocalMavenRepository(new File("."));
    PomArtifact pomArtifact;
    try (InputStream inputStream = MavenRepositoryTest.class.getResource("/groovy-3.0.7.pom").openStream()) {
      pomArtifact = repository.toArtifact(inputStream);
    }

    assertEquals("org.codehaus.groovy", pomArtifact.getGroupId());
    assertEquals("groovy", pomArtifact.getArtifactId());
    assertEquals("3.0.7", pomArtifact.getVersion());

    assertEquals(Collections.emptyList(), pomArtifact.getDependencyManagement());
    assertEquals(Arrays.asList(
        dep("com.thoughtworks.xstream", "xstream", "1.4.14", Scope.RUNTIME, true,
            exclusion("junit", "junit"),
            exclusion("xpp3_min", "xpp3"),
            exclusion("xmlpull", "xmlpull"),
            exclusion("jmock", "jmock")),
        dep("org.fusesource.jansi", "jansi", "1.18", Scope.RUNTIME, true),
        dep("org.apache.ivy", "ivy", "2.5.0", Scope.RUNTIME, true,
            exclusion("*", "*")),
        dep("org.codehaus.gpars", "gpars", "1.2.1", Scope.RUNTIME, true,
            exclusion("groovy-all", "org.codehaus.groovy")),
        dep("com.tunnelvisionlabs", "antlr4-runtime", "4.7.4", Scope.RUNTIME, true)
    ), pomArtifact.getDependencies());
  }

  private Dependency dep(String groupId, String artifactId, String version, Artifact... exclusions) {
    return dep(groupId, artifactId, version, null, exclusions);
  }

  private Dependency dep(String groupId, String artifactId, String version, Scope scope, Artifact... exclusions) {
    return dep(groupId, artifactId, version, scope, false, exclusions);
  }

  private Dependency dep(String groupId, String artifactId, String version, Scope scope, boolean optional, Artifact... exclusions) {
    Dependency dependency = new Dependency();
    dependency.setGroupId(groupId);
    dependency.setArtifactId(artifactId);
    dependency.setVersion(version);
    dependency.setScope(scope);
    dependency.setOptional(optional);

    dependency.setExclusions(Arrays.asList(exclusions));
    return dependency;
  }

  private Artifact exclusion(String groupId, String artifactId) {
    Artifact artifact = new Artifact();
    artifact.setGroupId(groupId);
    artifact.setArtifactId(artifactId);
    return artifact;
  }
}

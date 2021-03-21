package com.tambapps.maven.dependency.resolver.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class RemoteMavenRepositoryTest {

  private final RemoteMavenRepository repository = new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL);

  @Test
  @Ignore
  public void existsTest() throws IOException {
    assertTrue(repository.exists("com.google.code.gson", "gson", "2.2.4"));
    assertTrue(repository.exists("com.google.code.gson:gson:2.2.4"));
  }

  @Test
  @Ignore
  public void retrieveArtifactJar() throws IOException {
    try (InputStream is = repository.retrieveArtifactJar("com.google.code.gson", "gson", "2.2.4")) {
      assertNotNull(is);
    }
  }

  @Test
  @Ignore
  public void retrieveArtifact() throws IOException {
    Artifact artifact = repository.retrieveArtifact("com.google.code.gson", "gson", "2.2.4");
    assertEquals("com.google.code.gson", artifact.getGroupId());
    assertEquals("gson", artifact.getArtifactId());
    assertEquals("2.2.4", artifact.getVersion());
    assertEquals(1, artifact.getDependencies().size());
    Dependency junitDependency = artifact.getDependencies().get(0);
    assertEquals("junit", junitDependency.getGroupId());
    assertEquals("junit", junitDependency.getArtifactId());
    assertEquals("3.8.2", junitDependency.getVersion());

    Artifact parentArtifact = artifact.getParent();
    assertNotNull(parentArtifact);
    assertEquals("org.sonatype.oss", parentArtifact.getGroupId());
    assertEquals("oss-parent", parentArtifact.getArtifactId());
    assertEquals("7", parentArtifact.getVersion());
    assertTrue(parentArtifact.getDependencies().isEmpty());
    assertTrue(parentArtifact.getDependencyManagement().isEmpty());
  }
}

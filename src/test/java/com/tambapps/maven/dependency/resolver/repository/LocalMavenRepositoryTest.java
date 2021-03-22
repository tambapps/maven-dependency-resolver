package com.tambapps.maven.dependency.resolver.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class LocalMavenRepositoryTest {

  private final LocalMavenRepository repository = new LocalMavenRepository(new File(new File(System.getProperty("user.home")), ".m2"));

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
    PomArtifact pomArtifact = repository.retrieveArtifact("com.google.code.gson", "gson", "2.2.4");
    assertEquals("com.google.code.gson", pomArtifact.getGroupId());
    assertEquals("gson", pomArtifact.getArtifactId());
    assertEquals("2.2.4", pomArtifact.getVersion());
    assertEquals(1, pomArtifact.getDependencies().size());
    Dependency junitDependency = pomArtifact.getDependencies().get(0);
    assertEquals("junit", junitDependency.getGroupId());
    assertEquals("junit", junitDependency.getArtifactId());
    assertEquals("3.8.2", junitDependency.getVersion());

    PomArtifact parentPomArtifact = pomArtifact.getParent();
    assertNotNull(parentPomArtifact);
    assertEquals("org.sonatype.oss", parentPomArtifact.getGroupId());
    assertEquals("oss-parent", parentPomArtifact.getArtifactId());
    assertEquals("7", parentPomArtifact.getVersion());
    assertTrue(parentPomArtifact.getDependencies().isEmpty());
    assertTrue(parentPomArtifact.getDependencyManagement().isEmpty());
  }
}

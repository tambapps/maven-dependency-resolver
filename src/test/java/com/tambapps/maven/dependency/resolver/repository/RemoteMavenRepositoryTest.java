package com.tambapps.maven.dependency.resolver.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;

import com.tambapps.maven.dependency.resolver.storage.RemoteStorage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class RemoteMavenRepositoryTest {

  private final MavenRepository repository = new MavenRepository(new RemoteStorage());

  @Test
  public void existsTest() throws IOException {
    assertTrue(repository.exists("com.google.code.gson", "gson", "2.2.4"));
    assertTrue(repository.exists("com.google.code.gson:gson:2.2.4"));
  }

  @Test
  public void retrieveArtifactJar() throws IOException {
    try (InputStream is = repository.retrieveArtifactJar("com.google.code.gson", "gson", "2.2.4")) {
      assertNotNull(is);
    }
  }

  @Test
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

  @Test
  public void testJitPack() throws IOException {
    final MavenRepository repository = new MavenRepository(new RemoteStorage("https://jitpack.io"));
    assertTrue(repository.exists("com.github.tambapps", "hyperpoet", "v1.0.0"));
    PomArtifact pomArtifact = repository.retrieveArtifact("com.github.tambapps", "hyperpoet", "v1.0.0");
    System.out.println(pomArtifact);
  }
}

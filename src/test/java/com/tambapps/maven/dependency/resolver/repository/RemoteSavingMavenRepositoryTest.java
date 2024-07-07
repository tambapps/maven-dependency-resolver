package com.tambapps.maven.dependency.resolver.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.storage.RemoteStorage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class RemoteSavingMavenRepositoryTest {

  private static final Artifact ARTIFACT = new Artifact("com.google.code.gson", "gson", "2.2.4");
  private final RemoteSavingMavenRepository repository = new RemoteSavingMavenRepository(
      new File(new File(System.getProperty("user.home")), ".m2"),
      Collections.singletonList(new MavenRepository(new RemoteStorage()))
  );

  @Test
  public void testGsonExists() throws IOException {
    repository.deleteArtifact(ARTIFACT);
    assertTrue(repository.exists(ARTIFACT.toArtifactString()));
  }

  @Test
  public void testExistsLocally() throws IOException {
    repository.deleteArtifact(ARTIFACT);
    assertFalse(repository.existsLocally(ARTIFACT.toArtifactString()));
    assertFalse(repository.existsLocally(ARTIFACT.getGroupId(), ARTIFACT.getArtifactId(), ARTIFACT.getVersion()));
    assertFalse(repository.existsLocally(ARTIFACT));

    repository.retrieveArtifact(ARTIFACT);
    assertTrue(repository.existsLocally(ARTIFACT.toArtifactString()));
    assertTrue(repository.existsLocally(ARTIFACT.getGroupId(), ARTIFACT.getArtifactId(), ARTIFACT.getVersion()));
    assertTrue(repository.existsLocally(ARTIFACT));
  }


  @Test
  public void testGetGson() throws IOException {
    repository.deleteArtifact(ARTIFACT);

    Artifact artifact = repository.retrieveArtifact(ARTIFACT);

    assertEquals("com.google.code.gson", artifact.getGroupId());
    assertEquals("gson", artifact.getArtifactId());
    assertEquals("2.2.4", artifact.getVersion());
  }
}

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

public class RemoteSavingMavenRepositoryTest {

  private final LocalMavenRepository localRepository =
      new LocalMavenRepository(new File(new File(System.getProperty("user.home")), ".m2"));
  private final RemoteSavingMavenRepository repository = new RemoteSavingMavenRepository(
      localRepository.root,
      Arrays.asList(new MavenRepository(new RemoteStorage()))
  );

  @Test
  public void testGsonExists() throws IOException {
    localRepository.deleteArtifact(new Artifact("com.google.code.gson", "gson", "2.2.4"));
    assertFalse(localRepository.exists("com.google.code.gson:gson:2.2.4"));
    assertTrue(repository.exists("com.google.code.gson:gson:2.2.4"));
  }


  @Test
  public void testGetGson() throws IOException {
    localRepository.deleteArtifact(new Artifact("com.google.code.gson", "gson", "2.2.4"));

    Artifact artifact = repository.retrieveArtifact("com.google.code.gson", "gson", "2.2.4");

    assertEquals("com.google.code.gson", artifact.getGroupId());
    assertEquals("gson", artifact.getArtifactId());
    assertEquals("2.2.4", artifact.getVersion());
    assertTrue(localRepository.exists("com.google.code.gson:gson:2.2.4"));
  }
}

package com.tambapps.maven.dependency.resolver.repository;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class RemoteMavenRepositoryTest {

  private final RemoteMavenRepository repository = new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL);

  @Test
  @Ignore
  public void existsTest() throws IOException {
    assertTrue(repository.exists("com.google.code.gson", "gson", "2.2.4"));
    assertTrue(repository.exists("com.google.code.gson:gson:2.2.4"));
  }
}

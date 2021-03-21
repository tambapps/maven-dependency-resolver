package com.tambapps.maven.dependency.resolver.repository;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LocalRepositoryTest {

  private final LocalRepository repository = new LocalRepository(new File(new File(System.getProperty("user.home")), ".m2"));

  @Test
  @Ignore
  public void existsTest() throws IOException {
    assertTrue(repository.exists("com.google.code.gson", "gson", "2.2.4"));
    assertTrue(repository.exists("com.google.code.gson:gson:2.2.4"));
  }
}

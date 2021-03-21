package com.tambapps.maven.dependency.resolver;

import static org.junit.Assert.assertTrue;

import com.tambapps.maven.dependency.resolver.repository.LocalRepository;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class LocalRepositoryTest {

  private final LocalRepository repository = new LocalRepository(new File(new File(System.getProperty("user.home")), ".m2"));

  @Test
  @Ignore
  public void existsTest() {
    assertTrue(repository.exists("com.google.code.gson", "gson", "2.2.4"));
    assertTrue(repository.exists("com.google.code.gson:gson:2.2.4"));
  }
}

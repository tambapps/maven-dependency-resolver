package com.tambapps.maven.dependency.resolver.storage;

import com.tambapps.maven.dependency.resolver.exception.ResourceNotFound;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
public class LocalRepositoryStorage implements RepositoryStorage {

  protected final File repoRoot;

  @Override
  public boolean exists(String resourcePath) throws IOException {
    return new File(repoRoot, resourcePath).exists();
  }

  @Override
  public InputStream get(String resourcePath) throws IOException {
    File file = new File(repoRoot, resourcePath);
    if (!file.exists()) {
      throw new ResourceNotFound();
    }
    return new FileInputStream(file);
  }

}

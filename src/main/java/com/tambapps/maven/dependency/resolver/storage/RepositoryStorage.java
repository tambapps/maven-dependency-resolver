package com.tambapps.maven.dependency.resolver.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstraction of the way to extract resource.
 * Can be local storage, an HTTP request, ...
 */
public interface RepositoryStorage {

  boolean exists(String resourcePath) throws IOException;

  InputStream get(String resourcePath) throws IOException;
}

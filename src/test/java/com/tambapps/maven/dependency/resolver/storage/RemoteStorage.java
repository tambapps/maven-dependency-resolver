package com.tambapps.maven.dependency.resolver.storage;

import com.tambapps.maven.dependency.resolver.exception.ResourceNotFound;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class RemoteStorage implements RepositoryStorage {

  public static final String MAVEN_REPO_URL = "https://repo1.maven.org/maven2";

  private final OkHttpClient client = new OkHttpClient();
  private final String repoUrl;

  public RemoteStorage() {
    this(MAVEN_REPO_URL);
  }

  @Override
  public boolean exists(String resourcePath) throws IOException {
    try (Response response = fetch(resourcePath)) {
      return response.isSuccessful();
    }
  }

  @Override
  public InputStream get(String resourcePath) throws IOException {
    Response response = fetch(resourcePath);
    return responseStream(response, resourcePath);
  }

  private Response fetch(String resourcePath) throws IOException {
    return client.newCall(new Request.Builder().url(repoUrl + "/" + resourcePath).build()).execute();
  }

  private InputStream responseStream(Response response, String resourcePath)
      throws IOException {
    if (response.isSuccessful()) {
      return new ResponseBodyInputStream(response);
    } else if (response.code() == 404) {
      throw new ResourceNotFound();
    } else {
      throw new IOException(String.format("Requesting resource %s failed: %s",resourcePath, response.message()));
    }
  }

  static class ResponseBodyInputStream extends InputStream {

    private final Response response;
    private final InputStream inputStream;

    ResponseBodyInputStream(Response response) {
      this.response = response;
      this.inputStream = response.body().byteStream();
    }

    @Override
    public int read() throws IOException {
      return inputStream.read();
    }

    @Override
    public int available() throws IOException {
      return inputStream.available();
    }

    @Override
    public boolean markSupported() {
      return inputStream.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
      inputStream.mark(readlimit);
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
      return inputStream.read(b);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
      return inputStream.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
      inputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
      return inputStream.skip(n);
    }

    @Override
    public void close() throws IOException {
      try {
        inputStream.close();
      } finally {
        response.close();
      }
    }
  }
}

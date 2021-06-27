package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class RemoteMavenRepository extends AbstractMavenRepository {

  public static final String MAVEN_REPO_URL = "https://repo1.maven.org/maven2";
  private final OkHttpClient client;
  private final String repoUrl;

  public RemoteMavenRepository(OkHttpClient client, String repoUrl) {
    this.client = client;
    this.repoUrl = repoUrl.endsWith("/") ? repoUrl : repoUrl + "/";
  }

  public RemoteMavenRepository(String repoUrl) {
    this(new OkHttpClient(), repoUrl);
  }

  @Override
  public boolean exists(String groupId, String artifactId, String version) throws IOException  {
    Request request = pomRequest(groupId, artifactId, version).get().build();
    try (Response response = client.newCall(request).execute()) {
      switch (response.code()) {
        case 200:
          return true;
        case 404:
          return false;
        default:
          throw new IOException(String.format("Requesting artifact %s:%s:%s failed: %s",groupId, artifactId, version, response.message()));
      }
    }
  }

  private Request.Builder jarRequest(String groupId, String artifactId, String version) {
    return new Request.Builder().url(repoUrl + getJarKey(groupId, artifactId, version));
  }

  private Request.Builder pomRequest(String groupId, String artifactId, String version) {
    return new Request.Builder().url(repoUrl + getPomKey(groupId, artifactId, version));
  }

  @Override
  public InputStream retrieveArtifactJar(String groupId, String artifactId, String version)
      throws IOException {
    Request request = jarRequest(groupId, artifactId, version).get().build();
    // response will be closed when closing InputStream (see ResponseBodyInputStream)
    return responseStream(client.newCall(request).execute(), groupId, artifactId, version);
  }

  @Override
  public InputStream retrieveArtifactPom(String groupId, String artifactId, String version)
      throws IOException {
    Request request = pomRequest(groupId, artifactId, version).get().build();
    // response will be closed when closing InputStream (see ResponseBodyInputStream)
    return responseStream(client.newCall(request).execute(), groupId, artifactId, version);
  }

  private InputStream responseStream(Response response, String groupId, String artifactId, String version)
      throws IOException {
    if (response.isSuccessful()) {
      return new ResponseBodyInputStream(response);
    } else if (response.code() == 404) {
      throw new ArtifactNotFoundException(groupId, artifactId, version);
    } else {
      throw new IOException(String.format("Requesting artifact %s:%s:%s failed: %s",groupId, artifactId, version, response.message()));
    }
  }
  @Override
  public PomArtifact retrieveArtifact(String groupId, String artifactId, String version)
      throws IOException {
    Request request = pomRequest(groupId, artifactId, version).get().build();
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException(String.format("Requesting artifact %s:%s:%s failed: %s",groupId, artifactId, version, response.message()));
      }
      try (InputStream inputStream = response.body().byteStream()) {
        return toArtifact(inputStream);
      }
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

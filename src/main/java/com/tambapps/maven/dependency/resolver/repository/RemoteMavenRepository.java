package com.tambapps.maven.dependency.resolver.repository;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tambapps.maven.dependency.resolver.data.Artifact;

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
    Request request = jarRequest(groupId, artifactId, version).get().build();
    Response response = client.newCall(request).execute();
    switch (response.code()) {
      case 200:
        return true;
      case 404:
        return false;
      default:
        throw new IOException(String.format("Requesting artifact %s:%s:%s failed: %s",groupId, artifactId, version, response.message()));
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
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException(String.format("Requesting artifact %s:%s:%s failed: %s",groupId, artifactId, version, response.message()));
    }
    return response.body().byteStream();
  }

  @Override
  public InputStream retrieveArtifactPom(String groupId, String artifactId, String version)
      throws IOException {
    Request request = pomRequest(groupId, artifactId, version).get().build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException(String.format("Requesting artifact %s:%s:%s failed: %s",groupId, artifactId, version, response.message()));
    }
    return response.body().byteStream();
  }

  @Override
  public Artifact retrieveArtifact(String groupId, String artifactId, String version)
      throws IOException {
    Request request = pomRequest(groupId, artifactId, version).get().build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException(String.format("Requesting artifact %s:%s:%s failed: %s",groupId, artifactId, version, response.message()));
    }
    try (InputStream inputStream = response.body().byteStream()) {
      return toArtifact(inputStream);
    }
  }
}

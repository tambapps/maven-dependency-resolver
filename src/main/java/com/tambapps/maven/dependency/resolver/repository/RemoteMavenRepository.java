package com.tambapps.maven.dependency.resolver.repository;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

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
    Request request = request(groupId, artifactId, version).get().build();
    Response response = client.newCall(request).execute();
    return response.code() == 200;
  }

  private Request.Builder request(String groupId, String artifactId, String version) {
    return new Request.Builder().url(repoUrl + getJarKey(groupId, artifactId, version));
  }
}

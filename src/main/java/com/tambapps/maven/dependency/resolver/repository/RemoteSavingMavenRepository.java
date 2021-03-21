package com.tambapps.maven.dependency.resolver.repository;

import com.squareup.okhttp.OkHttpClient;
import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Repository that look for dependency locally, and if not found in remote repository.
 * all fetched dependencies are saved in local repository
 */
public class RemoteSavingMavenRepository extends RemoteMavenRepository {

  private final LocalMavenRepository localRepository;

  public RemoteSavingMavenRepository(OkHttpClient client, String repoUrl,
      LocalMavenRepository localRepository) {
    super(client, repoUrl);
    this.localRepository = localRepository;
  }

  public RemoteSavingMavenRepository(String repoUrl, LocalMavenRepository localRepository) {
    super(repoUrl);
    this.localRepository = localRepository;
  }

  @Override
  public boolean exists(String groupId, String artifactId, String version)
      throws IOException {
    return localRepository.exists(groupId, artifactId, version) ||
        super.exists(groupId, artifactId, version);
  }

  public boolean existsLocally(String dependencyString) {
    return localRepository.exists(dependencyString);
  }

  public boolean existsLocally(String groupId, String artifactId, String version) {
    return localRepository.exists(groupId, artifactId, version);
  }

  @Override
  public Artifact retrieveArtifact(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return localRepository.retrieveArtifact(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      Artifact artifact = super.retrieveArtifact(groupId, artifactId, version);
      localRepository.saveArtifactJar(artifact,
          retrieveArtifactJar(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
      localRepository.saveArtifactPom(artifact,
          retrieveArtifactPom(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
      return artifact;
    }
  }

  @Override
  public InputStream retrieveArtifactPom(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return localRepository.retrieveArtifactPom(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      return super.retrieveArtifactPom(groupId, artifactId, version);
    }
  }

  @Override
  public InputStream retrieveArtifactJar(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return localRepository.retrieveArtifactJar(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      return super.retrieveArtifactJar(groupId, artifactId, version);
    }
  }
}

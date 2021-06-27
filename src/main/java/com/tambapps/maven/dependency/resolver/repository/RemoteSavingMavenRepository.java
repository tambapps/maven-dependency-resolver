package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Repository that look for dependency locally, and if not found in remote repositories.
 * all fetched dependencies are saved in the local repository
 */
public class RemoteSavingMavenRepository extends LocalMavenRepository {

  // the first element of this list is the localRepository (this). This is to simplify code
  private final List<RemoteMavenRepository> remoteRepositories;

  public RemoteSavingMavenRepository(File root, List<RemoteMavenRepository> remoteRepositories) {
    super(root);
    this.remoteRepositories = remoteRepositories;
  }

  @SneakyThrows
  @Override
  public boolean exists(String groupId, String artifactId, String version) {
    if (super.exists(groupId, artifactId, version)) {
      return true;
    }
    for (MavenRepository repository : remoteRepositories) {
      if (repository.exists(groupId, artifactId, version)) {
        return true;
      }
    }
    return false;
  }

  public boolean existsLocally(String dependencyString) {
    return super.exists(dependencyString);
  }

  public boolean existsLocally(String groupId, String artifactId, String version) {
    return super.exists(groupId, artifactId, version);
  }

  @Override
  public PomArtifact retrieveArtifact(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return super.retrieveArtifact(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      for (RemoteMavenRepository remoteRepository : remoteRepositories) {
        try {
          saveArtifactPom(groupId, artifactId, version, remoteRepository.retrieveArtifactPom(groupId, artifactId, version));
          // jar may not exists, so let's not try to save it in this case
          break;
        } catch (ArtifactNotFoundException e1) {
          // just try with the next repository
        }
      }
      return super.retrieveArtifact(groupId, artifactId, version);
    }
  }

  @Override
  public InputStream retrieveArtifactPom(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return super.retrieveArtifactPom(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      for (RemoteMavenRepository remoteRepository : remoteRepositories) {
        try {
          saveArtifactPom(groupId, artifactId, version, remoteRepository.retrieveArtifactPom(groupId, artifactId, version));
          break;
        } catch (ArtifactNotFoundException e1) {
          // just try with the next repository
        }
      }
      return super.retrieveArtifactPom(groupId, artifactId, version);
    }
  }

  @Override
  public InputStream retrieveArtifactJar(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return super.retrieveArtifactJar(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      for (RemoteMavenRepository remoteRepository : remoteRepositories) {
        try {
          saveArtifactJar(groupId, artifactId, version, remoteRepository.retrieveArtifactJar(groupId, artifactId, version));
          break;
        } catch (ArtifactNotFoundException e1) {
          // just try with the next repository
        }
      }
      return super.retrieveArtifactJar(groupId, artifactId, version);
    }
  }
}

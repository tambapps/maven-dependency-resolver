package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository that look for dependency locally, and if not found in remote repositories.
 * all fetched dependencies are saved in the local repository
 */
public class RemoteSavingMavenRepository implements MavenRepository {

  @Getter
  private final LocalMavenRepository localRepository;
  // the first element of this list is the localRepository. This is to simplify code
  private final List<MavenRepository> allRepositories;

  public RemoteSavingMavenRepository(LocalMavenRepository localRepository,
      List<RemoteMavenRepository> remoteRepositories) {
    this.localRepository = localRepository;
    this.allRepositories = new ArrayList<>();
    allRepositories.add(localRepository);
    allRepositories.addAll(remoteRepositories);
  }

  @Override
  public boolean exists(String groupId, String artifactId, String version)
      throws IOException {
    for (MavenRepository repository : allRepositories) {
      if (repository.exists(groupId, artifactId, version)) {
        return true;
      }
    }
    return false;
  }

  public boolean existsLocally(String dependencyString) {
    return localRepository.exists(dependencyString);
  }

  public boolean existsLocally(String groupId, String artifactId, String version) {
    return localRepository.exists(groupId, artifactId, version);
  }

  @Override
  public PomArtifact retrieveArtifact(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return localRepository.retrieveArtifact(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      if (!retrieveFromRemoteRepositoriesAndSaveLocally(groupId, artifactId, version)) {
        throw new ArtifactNotFoundException(groupId, artifactId, version);
      }
      return localRepository.retrieveArtifact(groupId, artifactId, version);
    }
  }

  @Override
  public InputStream retrieveArtifactPom(String groupId, String artifactId, String version)
      throws IOException {
    for (MavenRepository repository : allRepositories) {
      try {
        return repository.retrieveArtifactPom(groupId, artifactId, version);
      } catch (ArtifactNotFoundException e) {
        // just try with the next repository
      }
    }
    throw new ArtifactNotFoundException(groupId, artifactId, version);
  }

  @Override
  public InputStream retrieveArtifactJar(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return localRepository.retrieveArtifactJar(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      if (!retrieveFromRemoteRepositoriesAndSaveLocally(groupId, artifactId, version)) {
        throw e;
      }
      return localRepository.retrieveArtifactJar(groupId, artifactId, version);
    }
  }

  // return true if found on one of the remote repositories
  private boolean retrieveFromRemoteRepositoriesAndSaveLocally(String groupId, String artifactId, String version)
      throws IOException {
    // skipping local repository
    for (int i = 1; i < allRepositories.size(); i++) {
      try {
        saveInLocalRepository(allRepositories.get(i), groupId, artifactId, version);
        return true;
      } catch (ArtifactNotFoundException e) {
        // just try with the next repository
      }
    }
    return false;
  }

  private void saveInLocalRepository(MavenRepository repository, String groupId, String artifactId, String version)
      throws IOException {
    localRepository.saveArtifactJar(groupId, artifactId, version,
        repository.retrieveArtifactJar(groupId, artifactId, version));
    localRepository.saveArtifactPom(groupId, artifactId, version,
        repository.retrieveArtifactPom(groupId, artifactId, version));
  }
}

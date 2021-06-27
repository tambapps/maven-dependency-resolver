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
    for (MavenRepository repository : allRepositories) {
      try {
        return repository.retrieveArtifact(groupId, artifactId, version);
      } catch (ArtifactNotFoundException e) {
        // just try with the next repository
      }
    }
    throw new ArtifactNotFoundException();
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
    throw new ArtifactNotFoundException();
  }

  @Override
  public InputStream retrieveArtifactJar(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return localRepository.retrieveArtifactJar(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      // skipping local repository
      for (int i = 1; i < allRepositories.size(); i++) {
        try {
          MavenRepository remoteRepository = allRepositories.get(i);
          localRepository.saveArtifactJar(groupId, artifactId, version,
              remoteRepository.retrieveArtifactJar(groupId, artifactId, version));
          localRepository.saveArtifactPom(groupId, artifactId, version,
              remoteRepository.retrieveArtifactPom(groupId, artifactId, version));
        } catch (ArtifactNotFoundException e1) {
          // just try with the next repository
        }
      }

      // now let's check if the artifact was downloaded in the local repository
      if (!localRepository.exists(groupId, artifactId, version)) {
        // if it weren't, it means that the artifact was not found in remote repositories
        throw e;
      }
      return localRepository.retrieveArtifactJar(groupId, artifactId, version);
    }
  }
}

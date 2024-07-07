package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.exception.ArtifactNotFoundException;
import com.tambapps.maven.dependency.resolver.storage.RepositoryStorage;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Repository that look for dependency locally, and if not found in remote repositories.
 * all fetched dependencies are saved in the local repository
 */
public class RemoteSavingMavenRepository extends LocalMavenRepository {

  // the first element of this list is the localRepository (this). This is to simplify code
  private final List<MavenRepository> remoteRepositories;

  public RemoteSavingMavenRepository(RepositoryStorage remoteStorage) {
    this(new File(System.getProperty("user.home"), ".m2"), remoteStorage);
  }

  public RemoteSavingMavenRepository(File root, RepositoryStorage remoteStorage) {
    this(root, Collections.singletonList(new MavenRepository(remoteStorage)));
  }
  public RemoteSavingMavenRepository(File root, List<MavenRepository> remoteRepositories) {
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

  public boolean existsLocally(String dependencyString) throws IOException {
    String[] fields = Artifact.extractFields(dependencyString);
    return repositoryStorage.exists(getPomKey(fields[0], fields[1], fields[2]));
  }

  public boolean existsLocally(String groupId, String artifactId, String version) throws IOException {
    return repositoryStorage.exists(getPomKey(groupId, artifactId, version));
  }

  public boolean existsLocally(Artifact artifact) throws IOException {
    return repositoryStorage.exists(getPomKey(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
  }

  @Override
  public PomArtifact retrieveArtifact(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return super.retrieveArtifact(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      for (MavenRepository remoteRepository : remoteRepositories) {
        try {
          saveRemoteArtifact(remoteRepository, groupId, artifactId, version);
          return super.retrieveArtifact(groupId, artifactId, version);
        } catch (ArtifactNotFoundException e1) {
          // just try with the next repository
        }
      }
      throw e;
    }
  }

  private void saveRemoteArtifact(MavenRepository remoteRepository, String groupId, String artifactId, String version) throws IOException {
    try (InputStream is = remoteRepository.retrieveArtifactPom(groupId, artifactId, version)) {
      saveArtifactPom(groupId, artifactId, version, is);
    }
    try (InputStream is = remoteRepository.retrieveArtifactJar(groupId, artifactId, version)) {
      saveArtifactJar(groupId, artifactId, version, is);
    } catch (ArtifactNotFoundException e) {
      // ignore. Some artifacts don't have JARs
    }
  }

  @Override
  public InputStream retrieveArtifactPom(String groupId, String artifactId, String version)
      throws IOException {
    try {
      return super.retrieveArtifactPom(groupId, artifactId, version);
    } catch (ArtifactNotFoundException e) {
      for (MavenRepository remoteRepository : remoteRepositories) {
        try {
          saveRemoteArtifact(remoteRepository, groupId, artifactId, version);
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
      for (MavenRepository remoteRepository : remoteRepositories) {
        try {
          saveRemoteArtifact(remoteRepository, groupId, artifactId, version);
          break;
        } catch (ArtifactNotFoundException e1) {
          // just try with the next repository
        }
      }
      return super.retrieveArtifactJar(groupId, artifactId, version);
    }
  }

}

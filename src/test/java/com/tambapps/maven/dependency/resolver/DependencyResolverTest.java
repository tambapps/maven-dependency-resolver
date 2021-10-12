package com.tambapps.maven.dependency.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.maven.dependency.resolver.data.DependencyResolvingResult;
import com.tambapps.maven.dependency.resolver.repository.MavenRepository;
import com.tambapps.maven.dependency.resolver.repository.RemoteMavenRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DependencyResolverTest {

  private final MavenRepository repository = new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL);
  private final DependencyResolver resolver = new DependencyResolver(repository);

  @Test
  public void fetchSpringBootStarterWeb() throws IOException {
    DependencyResolvingResult resolve = resolver.resolve("org.springframework.boot", "spring-boot-starter-web", "2.4.2");
    System.out.println(resolve);
    assertEquals(resolve.getFetchedArtifacts().size(), resolve.getArtifactVersionsMap().size());
  }

  // example with parent pom and dependency versions defined in parent pom
  @Test
  public void fetchGuava() throws IOException {
    DependencyResolvingResult resolve =
        resolver.resolve("com.google.guava", "guava", "30.1.1-jre");
    System.out.println(resolve);
  }

  @Test
  public void fetchGson() throws IOException {
    DependencyResolvingResult resolve =
        resolver.resolve("com.google.code.gson", "gson", "2.8.6");
    System.out.println(resolve);
  }

  @Test
  public void fetchGroovy() throws IOException {
    DependencyResolvingResult resolve =
        resolver.resolve("org.codehaus.groovy", "groovy", "3.0.9");
    System.out.println(resolve);
  }
}

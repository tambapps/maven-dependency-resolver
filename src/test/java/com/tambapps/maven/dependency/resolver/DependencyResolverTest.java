package com.tambapps.maven.dependency.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.maven.dependency.resolver.data.DependencyResolvingResult;
import com.tambapps.maven.dependency.resolver.repository.MavenRepository;
import com.tambapps.maven.dependency.resolver.storage.RemoteStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DependencyResolverTest {

  private final MavenRepository repository = new MavenRepository(new RemoteStorage());
  private final DependencyResolver resolver = new DependencyResolver(repository);

  @AfterEach
  public void reset() {
    resolver.reset();
  }

  @Test
  public void fetchSpringBootStarterWeb() throws IOException {
    resolver.resolve("org.springframework.boot", "spring-boot-starter-web", "2.4.2");
    DependencyResolvingResult resolve = resolver.getResults();
    System.out.println(resolve);
  }

  // example with parent pom and dependency versions defined in parent pom
  @Test
  public void fetchGuava() throws IOException {
    resolver.resolve("com.google.guava", "guava", "30.1.1-jre");
    DependencyResolvingResult resolve = resolver.getResults();
    System.out.println(resolve);
  }

  @Test
  public void fetchGson() throws IOException {
    resolver.resolve("com.google.code.gson", "gson", "2.8.6");
    DependencyResolvingResult resolve = resolver.getResults();
    System.out.println(resolve);
    System.out.println(resolve.getFetchedArtifacts().size());
  }

  @Test
  public void fetchGroovy() throws IOException {
    resolver.resolve("org.codehaus.groovy", "groovy", "3.0.9");
    DependencyResolvingResult resolve = resolver.getResults();
    System.out.println(resolve);
  }
}

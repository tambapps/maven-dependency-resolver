package com.tambapps.maven.dependency.resolver;

import static com.tambapps.maven.dependency.resolver.DependencyResolver.resolve;
import static org.junit.Assert.assertEquals;

import com.tambapps.maven.dependency.resolver.data.DependencyResolvingResult;
import com.tambapps.maven.dependency.resolver.repository.MavenRepository;
import com.tambapps.maven.dependency.resolver.repository.RemoteMavenRepository;
import org.junit.Test;

import java.io.IOException;

public class DependencyResolverTest {

  private final MavenRepository repository = new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL);
  // example with properties used for versions
  @Test
  public void fetchSpringBootStarterWeb() throws IOException {
    DependencyResolvingResult resolve =
        resolve(repository,
            "org.springframework.boot", "spring-boot-starter-web", "2.4.2");
    DependencyResolvingResult resolve2 = new DependencyResolver2(repository).resolve("org.springframework.boot", "spring-boot-starter-web", "2.4.2");
    System.out.println(resolve);
    assertEquals(resolve, resolve2);
  }

  // example with parent pom and dependency versions defined in parent pom
  @Test
  public void fetchGuava() throws IOException {
    DependencyResolvingResult resolve =
        resolve(new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL),
            "com.google.guava", "guava", "30.1.1-jre");
    System.out.println(resolve);
  }

  @Test
  public void fetchGson() throws IOException {
    DependencyResolvingResult resolve =
        resolve(new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL),
            "com.google.code.gson", "gson", "2.8.6");
    System.out.println(resolve);
  }
}

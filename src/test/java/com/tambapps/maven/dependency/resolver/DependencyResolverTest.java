package com.tambapps.maven.dependency.resolver;

import static com.tambapps.maven.dependency.resolver.DependencyResolver.resolve;

import com.tambapps.maven.dependency.resolver.data.DependencyResolvingResult;
import com.tambapps.maven.dependency.resolver.repository.RemoteMavenRepository;
import org.junit.Test;

import java.io.IOException;

public class DependencyResolverTest {

  // example with properties used for versions
  @Test
  public void fetchSpringBootStarterWeb() throws IOException {
    DependencyResolvingResult resolve =
        resolve(new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL),
            "org.springframework.boot", "spring-boot-starter-web", "2.4.2");
    System.out.println(resolve);
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

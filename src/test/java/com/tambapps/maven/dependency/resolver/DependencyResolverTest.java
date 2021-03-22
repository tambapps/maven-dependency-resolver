package com.tambapps.maven.dependency.resolver;

import static com.tambapps.maven.dependency.resolver.DependencyResolver.resolve;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.repository.RemoteMavenRepository;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class DependencyResolverTest {

  // example with properties used for versions
  @Test
  public void fetchSpringBootStarterWeb() throws IOException {
    List<Artifact> resolve =
        resolve(new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL),
            "org.springframework.boot", "spring-boot-starter-web", "2.4.2");
    System.out.println(resolve);
  }

  // example with parent pom and dependency versions defined in parent pom
  @Test
  public void fetchGuava() throws IOException {
    List<Artifact> resolve =
        resolve(new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL),
            "com.google.guava", "guava", "30.1.1-jre");
    System.out.println(resolve);
  }
}

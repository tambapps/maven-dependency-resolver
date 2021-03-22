package com.tambapps.maven.dependency.resolver;

import static com.tambapps.maven.dependency.resolver.DependencyResolver.resolve;

import com.tambapps.maven.dependency.resolver.data.BaseArtifact;
import com.tambapps.maven.dependency.resolver.repository.RemoteMavenRepository;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class DependencyResolverTest {

  @Test
  public void fetchSpringBootStarterWeb() throws IOException {
    List<BaseArtifact> resolve =
        resolve(new RemoteMavenRepository(RemoteMavenRepository.MAVEN_REPO_URL),
            "org.springframework.boot", "spring-boot-starter-web", "2.4.2");
    System.out.println(resolve);
  }
}

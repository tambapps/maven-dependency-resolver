package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.io.PomParser;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractMavenRepository implements MavenRepository {

  protected static final String JAR_SUFFIX = ".jar";
  protected static final String POM_SUFFIX = ".pom";

  private final PomParser pomParser = new PomParser(this);

  protected PomArtifact toArtifact(InputStream inputStream) throws IOException {
    return pomParser.parse(inputStream);
  }

  protected String getKey(String groupId, String artifactId, String version) {
    return groupId.replaceAll("\\.", "/") + "/" +
        artifactId + "/" + version + "/" + artifactId + "-" + version;
  }

  protected String getPomKey(String groupId, String artifactId, String version) {
    return getKey(groupId, artifactId, version) + POM_SUFFIX;
  }

  protected String getJarKey(String groupId, String artifactId, String version) {
    return getKey(groupId, artifactId, version) + JAR_SUFFIX;
  }

}

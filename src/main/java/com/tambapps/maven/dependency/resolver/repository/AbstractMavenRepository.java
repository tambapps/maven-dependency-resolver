package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMavenRepository implements MavenRepository {

  private final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

  @Override
  public boolean exists(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return exists(fields[0], fields[1], fields[2]);
  }

  @Override
  public InputStream retrieveArtifactJar(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifactJar(fields[0], fields[1], fields[2]);
  }

  @Override
  public Artifact retrieveArtifact(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifact(fields[0], fields[1], fields[2]);
  }

  protected Artifact toArtifact(InputStream pomStream) throws IOException {
    Document document = parse(pomStream);
    document.getElementsByTagName("dependencies");
    Artifact artifact = new Artifact();
    // TODO handle case where pom has a parent and groupId is defined in parent
    artifact.setGroupId(document.getElementsByTagName("groupId").item(0).getTextContent());
    artifact.setArtifactId(document.getElementsByTagName("artifactId").item(0).getTextContent());
    artifact.setVersion(document.getElementsByTagName("version").item(0).getTextContent());
    artifact.setDependencies(extractDependencies(document.getElementsByTagName("dependencies")));
    artifact.setDependencyManagement(extractDependencies(document.getElementsByTagName("dependencyManagement")));
    return artifact;
  }

  private List<Dependency> extractDependencies(NodeList dependencyNodes) {
    List<Dependency> dependencies = new ArrayList<>();

    for (int i = 0; i < dependencyNodes.getLength(); i++) {
      Element node = (Element) dependencyNodes.item(i);
      NodeList versionNodes = node.getElementsByTagName("version");
      dependencies.add(Dependency.builder()
          .groupId(node.getElementsByTagName("groupId").item(0).getTextContent())
          .artifactId(node.getElementsByTagName("artifactId").item(0).getTextContent())
          .version(versionNodes.getLength() == 0 ? null : node.getElementsByTagName("version")
              .item(0).getTextContent())
          .build());
    }
    return dependencies;
  }

  private Document parse(InputStream is) throws IOException {
    try {
      return dbFactory.newDocumentBuilder().parse(is);
    } catch (SAXException | ParserConfigurationException e) {
      throw new IOException(e);
    }
  }
  protected String getKey(String groupId, String artifactId, String version) {
    return groupId.replaceAll("\\.", "/") + "/" +
        artifactId.replaceAll("\\.", "/") + "/" + version;
  }

  protected String getPomKey(String groupId, String artifactId, String version) {
    return groupId.replaceAll("\\.", "/") + "/" +
        artifactId.replaceAll("\\.", "/") + "/" + version + "/" +
        artifactId + "-" + version + ".pom";
  }

  protected String getJarKey(String groupId, String artifactId, String version) {
    return groupId.replaceAll("\\.", "/") + "/" +
        artifactId.replaceAll("\\.", "/") + "/" + version + "/" +
        artifactId + "-" + version + ".pom";
  }

  private String[] extractFields(String dependencyString) {
    String[] fields = dependencyString.split(":");
    if (fields.length != 3) {
      throw new IllegalArgumentException("Argument should be in pattern artifactId:groupId:version");
    }
    return fields;
  }
}

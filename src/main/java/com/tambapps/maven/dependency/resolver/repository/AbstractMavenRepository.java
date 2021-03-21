package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
    //  only fetch parent when needed (meaning when a version of a dependency isn't specified

    // TODO sometimes groupId is only defined in parent:
    /*
    <parent>
    <groupId>com.tambapps.gmage</groupId>
    <artifactId>gmage</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>
  <artifactId>gmage-desktop</artifactId>
     */
    String groupId = getPropertyOrNull(document, "groupId");
    Element parentNode = getElementOrNull(document, "parent");

    if (groupId == null && parentNode != null) {
      groupId = getPropertyOrNull(parentNode, "groupId");
    }

    artifact.setGroupId(groupId);
    artifact.setArtifactId(document.getElementsByTagName("artifactId").item(0).getTextContent());
    artifact.setVersion(document.getElementsByTagName("version").item(0).getTextContent());
    artifact.setDependencies(extractDependencies(document.getElementsByTagName("dependencies")));
    artifact.setDependencyManagement(extractDependencies(document.getElementsByTagName("dependencyManagement")));
    return artifact;
  }

  private Element getElementOrNull(Document document, String tagName) {
    NodeList nodes = document.getElementsByTagName(tagName);
    return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
  }

  private String getPropertyOrNull(Document document, String tagName) {
    NodeList nodes = document.getElementsByTagName(tagName);
    return nodes.getLength() == 0 ? null :  nodes.item(0).getTextContent();
  }

  private String getPropertyOrNull(Element document, String tagName) {
    NodeList nodes = document.getElementsByTagName(tagName);
    return nodes.getLength() == 0 ? null :  nodes.item(0).getTextContent();
  }
  private List<Dependency> extractDependencies(NodeList dependencyNodes) {
    List<Dependency> dependencies = new ArrayList<>();

    for (int i = 0; i < dependencyNodes.getLength(); i++) {
      Element node = (Element) dependencyNodes.item(i);
      dependencies.add(Dependency.builder()
          .groupId(node.getElementsByTagName("groupId").item(0).getTextContent())
          .artifactId(node.getElementsByTagName("artifactId").item(0).getTextContent())
          .version(getPropertyOrNull(node, "version"))
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

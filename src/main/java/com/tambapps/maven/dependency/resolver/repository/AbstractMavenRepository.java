package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import com.tambapps.maven.dependency.resolver.data.Scope;
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
  public InputStream retrieveArtifactPom(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifactPom(fields[0], fields[1], fields[2]);
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

    if (parentNode != null) {
      // we should always fetch parent because if it declared dependencies (not dependenciesManagment)
      // they should always be fetched for the child pom
      String parentGroupId = getPropertyOrNull(parentNode, "groupId");
      String parentArtifactId = getPropertyOrNull(parentNode, "artifactId");
      String parentVersion = getPropertyOrNull(parentNode, "version");
      artifact.setParent(retrieveArtifact(parentGroupId, parentArtifactId, parentVersion));
    }
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

  private String getPropertyOrDefault(Element document, String tagName, String defaultValue) {
    NodeList nodes = document.getElementsByTagName(tagName);
    return nodes.getLength() == 0 ? defaultValue :  nodes.item(0).getTextContent();
  }
  private List<Dependency> extractDependencies(NodeList dependencyNodes) {
    List<Dependency> dependencies = new ArrayList<>();

    for (int i = 0; i < dependencyNodes.getLength(); i++) {
      Element node = (Element) dependencyNodes.item(i);
      Dependency dependency = new Dependency();
      dependency.setGroupId(getPropertyOrNull(node, "groupId"));
      dependency.setArtifactId(getPropertyOrNull(node, "artifactId"));
      dependency.setVersion(getPropertyOrNull(node, "version"));
      dependency.setScope(Scope.valueOf(getPropertyOrDefault(node, "scope", "compile").toUpperCase()));
      dependency.setOptional(getPropertyOrDefault(node, "optional", "false").equals("true"));
      dependencies.add(dependency);
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

  protected String[] extractFields(String dependencyString) {
    String[] fields = dependencyString.split(":");
    if (fields.length != 3) {
      throw new IllegalArgumentException("Argument should be in pattern artifactId:groupId:version");
    }
    return fields;
  }
}

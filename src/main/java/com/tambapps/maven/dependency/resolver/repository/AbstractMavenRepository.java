package com.tambapps.maven.dependency.resolver.repository;

import com.tambapps.maven.dependency.resolver.data.PomArtifact;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMavenRepository implements MavenRepository {

  private final DocumentBuilderFactory dbFactory;

  AbstractMavenRepository() {
    dbFactory = DocumentBuilderFactory.newInstance();
    // REQUIRED
    dbFactory.setIgnoringComments(true);
  }
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
  public PomArtifact retrieveArtifact(String dependencyString) throws IOException {
    String[] fields = extractFields(dependencyString);
    return retrieveArtifact(fields[0], fields[1], fields[2]);
  }

  protected PomArtifact toArtifact(InputStream pomStream) throws IOException {
    Document document = parse(pomStream);
    PomArtifact pomArtifact = new PomArtifact();

    Element propertyNode = getElementOrNull(document, "properties");
    Map<String, String> properties = new HashMap<>();
    if (propertyNode != null) {
      extractProperties(properties, propertyNode);
    }
    pomArtifact.setProperties(properties);
    String groupId = getPropertyOrNull(document, "groupId");
    String version = getPropertyOrNull(document, "version");
    Element parentNode = getElementOrNull(document, "parent");

    if (groupId == null && parentNode != null) {
      groupId = getPropertyOrNull(parentNode, "groupId");
    }
    if (version == null && parentNode != null) {
      version = getPropertyOrNull(parentNode, "version");
    }

    pomArtifact.setGroupId(groupId);
    pomArtifact.setArtifactId(getPropertyOrNull(document, "artifactId"));
    pomArtifact.setVersion(version);
    pomArtifact.setDependencies(extractDependencies(getElementOrNull(document, "dependencies")));
    pomArtifact.setDependencyManagement(extractDependencies(getElementOrNull(document, "dependencyManagement")));

    if (parentNode != null) {
      // we should always fetch parent because if it declared dependencies (not dependenciesManagment)
      // they should always be fetched for the child pom
      String parentGroupId = getPropertyOrNull(parentNode, "groupId");
      String parentArtifactId = getPropertyOrNull(parentNode, "artifactId");
      String parentVersion = getPropertyOrNull(parentNode, "version");
      pomArtifact.setParent(retrieveArtifact(parentGroupId, parentArtifactId, parentVersion));
    }
    return pomArtifact;
  }

  private Element getElementOrNull(Document document, String tagName) {
    return getElementOrNull(document.getFirstChild(), tagName);
  }

  private Element getElementOrNull(Node node, String tagName) {
    Node firstChild = node.getFirstChild();
    if (firstChild == null) {
      return null;
    }
    for (Node child = firstChild; child.getNextSibling() != null; child = child.getNextSibling()) {
      if (child.getNodeName().equals(tagName)) {
        return (Element) child;
      }
    }
    return null;
  }

  private String getPropertyOrNull(Document document, String tagName) {
    return getPropertyOrNull(document.getFirstChild(), tagName);
  }

  private String getPropertyOrNull(Node node, String tagName) {
    Element element = getElementOrNull(node, tagName);
    return element == null ? null : element.getTextContent();
  }

  private String getPropertyOrDefault(Node document, String tagName, String defaultValue) {
    String property = getPropertyOrNull(document, tagName);
    return property == null ? defaultValue : property;
  }

  private void extractProperties(Map<String, String> properties, Node propertiesNode) {
    Node firstChild = propertiesNode.getFirstChild();
    if (firstChild == null) {
      return;
    }
    for (Node child = firstChild; child.getNextSibling() != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        properties.put(child.getNodeName(), child.getTextContent());
      }
    }
  }

  private List<Dependency> extractDependencies(Node dependenciesNode) {
    List<Dependency> dependencies = new ArrayList<>();
    if (dependenciesNode == null) {
      return dependencies;
    }
    NodeList dependencyNodes = ((Element)dependenciesNode).getElementsByTagName("dependency");

    for (int i = 0; i < dependencyNodes.getLength(); i++) {
      Node node = dependencyNodes.item(i);
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
        artifactId.replaceAll("\\.", "/") + "/" + version + "/" +
        artifactId + "-" + version;
  }

  protected String getPomKey(String groupId, String artifactId, String version) {
    return getKey(groupId, artifactId, version) + ".pom";
  }

  protected String getJarKey(String groupId, String artifactId, String version) {
    return getKey(groupId, artifactId, version) + ".jar";
  }

  protected String[] extractFields(String dependencyString) {
    String[] fields = dependencyString.split(":");
    if (fields.length != 3) {
      throw new IllegalArgumentException("Argument should be in pattern artifactId:groupId:version");
    }
    return fields;
  }
}

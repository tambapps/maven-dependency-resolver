package com.tambapps.maven.dependency.resolver.io;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.data.Dependency;
import com.tambapps.maven.dependency.resolver.data.PomArtifact;
import com.tambapps.maven.dependency.resolver.data.Scope;
import com.tambapps.maven.dependency.resolver.repository.MavenRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class PomParser {

  private static final Pattern PROPERTY_REFERENCE_PATTERN = Pattern.compile("\\$\\{([^\\s{}]*)\\}");

  private final DocumentBuilderFactory dbFactory;
  private final MavenRepository repository;

  @SneakyThrows
  public PomParser(MavenRepository repository) {
    this.repository = repository;
    dbFactory = DocumentBuilderFactory.newInstance();
    // REQUIRED
    dbFactory.setIgnoringComments(true);
    try {
      dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    } catch (ParserConfigurationException e) {
      // the feature isn't supported. Ok, just proceed
    }
  }

  public PomArtifact parse(InputStream pomStream) throws IOException {
    Document document = parseInputStream(pomStream);
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
      pomArtifact.setParent(repository.retrieveArtifact(parentGroupId, parentArtifactId, parentVersion));
    }
    resolveProperties(pomArtifact);
    return pomArtifact;
  }

  private void resolveProperties(PomArtifact pomArtifact) {
    resolveProperties(pomArtifact, pomArtifact);
    Map<String, String> properties = pomArtifact.getProperties();
    properties.put("project.groupId", pomArtifact.getGroupId());
    properties.put("project.artifactId", pomArtifact.getArtifactId());
    properties.put("project.version", pomArtifact.getVersion());
    PomArtifact parentPom = pomArtifact.getParent();
    if (parentPom != null) {
      properties.put("project.parent.groupId", parentPom.getGroupId());
      properties.put("project.parent.artifactId", parentPom.getArtifactId());
      properties.put("project.parent.version", parentPom.getVersion());
    }
    pomArtifact.getDependencyManagement().forEach(dep -> resolveProperties(pomArtifact, dep));
    pomArtifact.getDependencies().forEach(dep -> resolveProperties(pomArtifact, dep));
  }

  private void resolveProperties(PomArtifact pomArtifact, Artifact artifact) {
    if (artifact.getVersion() == null) {
      return;
    }
    Matcher matcher = PROPERTY_REFERENCE_PATTERN.matcher(artifact.getVersion());
    while (matcher.find()) {
      String propertyName = matcher.group(1);
      String propertyValue = pomArtifact.getProperty(propertyName);
      if (propertyValue != null) {
        artifact.setVersion(propertyValue);
      }
      matcher = PROPERTY_REFERENCE_PATTERN.matcher(artifact.getVersion());
    }
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
      dependency.setOptional(Boolean.parseBoolean(getPropertyOrNull(node, "optional")));
      dependency.setExclusions(extractDependencyExclusions(node));
      dependencies.add(dependency);
    }
    return dependencies;
  }

  private List<Artifact> extractDependencyExclusions(Node dependencyNode) {
    Element exclusions = getElementOrNull(dependencyNode, "exclusions");
    if (exclusions == null) {
      return Collections.emptyList();
    }
    List<Artifact> excludedArtifacts = new ArrayList<>();
    NodeList exclusionNodes = exclusions.getElementsByTagName("exclusion");

    for (int i = 0; i < exclusionNodes.getLength(); i++) {
      Node node = exclusionNodes.item(i);
      Artifact artifact = new Artifact();
      artifact.setGroupId(getPropertyOrNull(node, "groupId"));
      artifact.setArtifactId(getPropertyOrNull(node, "artifactId"));
      excludedArtifacts.add(artifact);
    }
    return excludedArtifacts;
  }

  private Document parseInputStream(InputStream is) throws IOException {
    try {
      return dbFactory.newDocumentBuilder().parse(is);
    } catch (SAXException | ParserConfigurationException e) {
      throw new IOException(e);
    }
  }

}

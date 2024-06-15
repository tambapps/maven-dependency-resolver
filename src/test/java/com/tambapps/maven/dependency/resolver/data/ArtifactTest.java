package com.tambapps.maven.dependency.resolver.data;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArtifactTest {

  @ParameterizedTest
  @MethodSource
  void testMatches(String a1, String a2) {
    Artifact artifact1 = Artifact.from(a1);
    Artifact artifact2 = Artifact.from(a2);
    assertTrue(artifact1.matches(artifact2), artifact1 + " should match " + artifact2);
    assertTrue(artifact2.matches(artifact1), artifact1 + " should match " + artifact1);
  }

  private static Stream<Arguments> testMatches() {
    return Stream.of(
        Arguments.of("org.jetbrains.kotlin:kotlin-stdlib:*", "org.jetbrains.kotlin:kotlin-stdlib:*"),
        Arguments.of("org.jetbrains.kotlin:kotlin-stdlib:*", "org.jetbrains.kotlin:kotlin-stdlib:1.23"),
        Arguments.of("org.jetbrains.kotlin:kotlin-stdlib:*", "org.jetbrains.kotlin:kotlin-stdlib:0.5"),
        Arguments.of("org.jetbrains.kotlin:kotlin-stdlib:5.5", "org.jetbrains.kotlin:kotlin-stdlib:5.5"),
        Arguments.of("org.jetbrains.kotlin:kotlin-stdlib:5.5", "org.jetbrains.kotlin:kotlin-stdlib:1.23"),
        Arguments.of("org.jetbrains.kotlin:*:*", "org.jetbrains.kotlin:kotlin-stdlib:*"),
        Arguments.of("org.jetbrains.kotlin:*:*", "org.jetbrains.kotlin:kotlin-compiler:*"),
        Arguments.of("org.jetbrains.kotlin:*:*", "org.jetbrains.kotlin:kotlin-stdlib:5.5")
        );
  }

  @ParameterizedTest
  @MethodSource
  void testDoesntMatches(String a1, String a2) {
    Artifact artifact1 = Artifact.from(a1);
    Artifact artifact2 = Artifact.from(a2);
    assertFalse(artifact1.matches(artifact2), artifact1 + " shouldn't match " + artifact2);
    assertFalse(artifact2.matches(artifact1), artifact1 + " shouldn't match " + artifact1);
  }

  private static Stream<Arguments> testDoesntMatches() {
    return Stream.of(
        Arguments.of("org.jetbrains.kotlin:kotlin-stdlib:*", "org.jetbrains.kotlin:kotlin-compiler:*"),
        Arguments.of("org.jetbrains.kotlin:kotlin-stdlib:*", "org.jetbrains.kotlin:kotlin-compiler:1.23"),
        Arguments.of("org.jetbrains.kotlin:*:*", "com.tambapps.marcel:kotlin-compiler:*")
        );
  }
}

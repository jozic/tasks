import AssemblyKeys._

organization := "jozic"

name := "tasks"

version := "0.3"

crossPaths := false

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings")

scalaVersion := "2.10.4"

libraryDependencies += "jline" % "jline" % "2.10"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

assemblySettings

assembleArtifact in packageScala := false

jarName in assembly := s"${name.value}-${version.value}.jar"

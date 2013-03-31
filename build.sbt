import AssemblyKeys._

organization := "jozic"

name := "tasks"

version := "0.2"

crossPaths := false

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings")

scalaVersion := "2.10.1"

libraryDependencies += "jline" % "jline" % "2.10"

assemblySettings

assembleArtifact in packageScala := false

jarName in assembly := s"${name.value}-${version.value}.jar"

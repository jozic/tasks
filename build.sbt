import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._


organization := "com.daodecode"

name := "tasks"

version := "0.4.1"

crossPaths := false

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings")

scalaVersion := "2.11.2"

libraryDependencies += "jline" % "jline" % "2.12"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2" % "test"

assemblySettings

assembleArtifact in packageScala := false

jarName in assembly := s"${name.value}-${version.value}.jar"

initialCommands in console := "import com.daodecode.tasks._"

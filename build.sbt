import play.PlayScala

scalaVersion := "2.11.1"

name := """Livrarium"""

version := "0.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "0.8.0",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc4",
  "com.mohiva" %% "play-silhouette" % "2.0-SNAPSHOT",
  "org.scaldi" %% "scaldi-play" % "0.4.1",
  "com.sksamuel.scrimage" %% "scrimage-core" % "1.4.1",
  "com.sksamuel.scrimage" %% "scrimage-canvas" % "1.4.1",
  "org.bouncycastle" % "bcprov-jdk16" % "1.45",
  "org.apache.pdfbox" % "pdfbox" % "1.8.6"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)


import play.PlayScala

scalaVersion := "2.11.1"

name := """Livrarium"""

version := "0.1"

libraryDependencies ++= Seq(
  "com.novus" %% "salat" % "1.9.8",
  "com.mohiva" %% "play-silhouette" % "1.0",
  "org.scaldi" %% "scaldi-play" % "0.4.1",
  "com.sksamuel.scrimage" %% "scrimage-core" % "1.4.1",
  "com.sksamuel.scrimage" %% "scrimage-canvas" % "1.4.1",
  "org.bouncycastle" % "bcprov-jdk16" % "1.45",
  "org.apache.pdfbox" % "pdfbox" % "1.8.6",
  cache
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

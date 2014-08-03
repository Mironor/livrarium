import play.PlayScala

scalaVersion := "2.11.1"

name := """Livrarium"""

version := "0.1"

libraryDependencies ++= Seq(
  "com.novus" %% "salat" % "1.9.8",
  "com.mohiva" %% "play-silhouette" % "1.0",
  "org.scaldi" %% "scaldi-play" % "0.4.1",
  cache
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

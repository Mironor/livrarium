name := """Livrarium"""

version := "0.1"

libraryDependencies ++= Seq(
  "se.radley" %% "play-plugins-salat" % "1.4.0",
  "com.typesafe" %% "play-plugins-mailer" % "2.1-RC2",
  "ws.securesocial" %% "securesocial" % "2.1.3"
)

coffeescriptOptions := Seq("bare")

play.Project.playScalaSettings

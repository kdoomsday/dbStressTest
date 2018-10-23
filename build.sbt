resolvers += Resolver.mavenLocal

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "dbStress",
    libraryDependencies ++= doobieDeps,
    libraryDependencies ++= monixDeps,
    libraryDependencies ++= http4sDeps,
    libraryDependencies ++= fs2Deps,
    libraryDependencies ++= testDeps,
    libraryDependencies ++= circeDeps,

    scalacOptions += "-Ypartial-unification",
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
  )

// Versiones para los paquetes
// val Http4sVersion  = "0.18.20"
val Http4sVersion  = "0.19.0"
val Specs2Version  = "4.1.0"
val LogbackVersion = "1.2.3"
val Fs2Version     = "1.0.0"
val MonixVersion   = "3.0.0-RC1"
val doobieVersion  = "0.6.0"
val circeVersion   = "0.10.0"

lazy val doobieDeps = Seq(
  "org.tpolecat" %% "doobie-core"   % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,

  "org.xerial"     % "sqlite-jdbc" % "3.25.2",
  "org.postgresql" % "postgresql"  % "9.4-1200-jdbc41"
)


// Listas de dependencias
lazy val monixDeps = Seq(
  "io.monix" %% "monix" % MonixVersion
)

lazy val http4sDeps = Seq(
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
)

lazy val testDeps = Seq(
  "org.specs2"  %% "specs2-core" % Specs2Version % "test",
  "com.lihaoyi" %% "utest"       % "0.6.5"       % "test"
)

lazy val fs2Deps = Seq(
  "co.fs2" %% "fs2-core" % Fs2Version
)

lazy val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic"
).map(_ % circeVersion)

// Resolver problemas generando el jar
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

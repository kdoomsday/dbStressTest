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
    libraryDependencies ++= testDeps,

    scalacOptions += "-Ypartial-unification",
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
  )

lazy val doobieVersion = "0.5.3"
lazy val doobieDeps = Seq(
  "org.tpolecat" %% "doobie-core"   % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,

  "org.xerial"     % "sqlite-jdbc" % "3.25.2",
  "org.postgresql" % "postgresql"  % "9.4-1200-jdbc41"
)

lazy val monixVersion = "3.0.0-RC1"
lazy val monixDeps = Seq(
  "io.monix" %% "monix" % monixVersion
)

val Http4sVersion = "0.18.19"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"
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


assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

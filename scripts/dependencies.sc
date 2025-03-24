import mill._, scalalib._

val circeVersion = "0.14.12"
val zioVersion = "2.1.16"
val zioHttpVersion = "3.1.0"
val tapirVersion = "1.2.10"
val flywayVersion = "11.4.0"
val zioInteropVersion = "23.1.0.5"
val doobieVersion = "1.0.0-RC7"
val protoQuillVersion = "4.6.0.1"
val postgresqlVersion = "42.3.1"

val stags = Agg(
  ivy"com.lihaoyi::scalatags:0.13.1",
  ivy"com.lihaoyi::mainargs:0.7.6"
)

val zio = Agg(
  "zio",
  "zio-streams"
).map(x => ivy"dev.zio::$x:$zioVersion") ++
  Agg(ivy"dev.zio::zio-interop-cats:$zioInteropVersion") ++
  Agg(ivy"dev.zio::zio-http:$zioHttpVersion")

val doobie = Agg(
  "doobie-core",
  "doobie-postgres",
  "doobie-postgres-circe",
  "doobie-specs2",
  "doobie-hikari"
).map(x => ivy"org.tpolecat::$x:$doobieVersion")

val flyway = Agg(ivy"org.flywaydb:flyway-core:$flywayVersion")
val flywayPostgres = Agg(
  ivy"org.flywaydb:flyway-database-postgresql:$flywayVersion"
)

val circe = Agg("circe-core", "circe-generic", "circe-parser").map(x =>
  ivy"io.circe::$x:$circeVersion"
)

val tapir = Agg(
  "tapir-core",
  "tapir-json-circe",
  "tapir-redoc-bundle"
).map(x => ivy"com.softwaremill.sttp.tapir::$x:$tapirVersion")

val quill = Agg(
  ivy"io.getquill::quill-jdbc-zio:$protoQuillVersion",
  ivy"io.getquill::quill-doobie:$protoQuillVersion",
  ivy"org.postgresql:postgresql:$postgresqlVersion"
)

val testLib = Agg(ivy"com.lihaoyi::utest:0.8.5")
val testFramework = "utest.runner.Framework"

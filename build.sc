import mill._, scalalib._
import $file.scripts.dependencies
import dependencies._

trait Base extends ScalaModule {
  def scalaVersion = "3.3.5"
  def scalacOptions = Seq("-Xmax-inlines", "55")
  override def ammoniteVersion = "3.0.2"
  override def prependShellScript: T[String] = ""
}

object cqrs extends Base {
  def ivyDeps = stags ++ testLib ++ zio ++ circe ++ tapir ++ doobie ++ quill
  def mainClass = Some("com.iict.app.Main")
}

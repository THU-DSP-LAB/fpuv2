import mill._
import scalalib._

def scalaVersions(chiselVersion: String) = chiselVersion match {
  case "chisel"  => "2.13.12"
  case "chisel3" => "2.13.10"
}

def defaultVersions(chiselVersion: String) = chiselVersion match {
  case "chisel" => Map(
    "chisel"        -> ivy"org.chipsalliance::chisel:6.4.0",
    "chisel-plugin" -> ivy"org.chipsalliance:::chisel-plugin:6.4.0",
    "chiseltest"    -> ivy"edu.berkeley.cs::chiseltest:6.0.0"
  )
  case "chisel3" => Map(
    "chisel"        -> ivy"edu.berkeley.cs::chisel3:3.6.0",
    "chisel-plugin" -> ivy"edu.berkeley.cs:::chisel3-plugin:3.6.0",
    "chiseltest"    -> ivy"edu.berkeley.cs::chiseltest:0.6.2"
  )
}

trait HasChisel extends SbtModule with Cross.Module[String] {
  def chiselModule: Option[ScalaModule] = None
  def chiselPluginJar: T[Option[PathRef]] = None
  def chiselIvy: Option[Dep] = Some(defaultVersions(crossValue)("chisel"))
  def chiselPluginIvy: Option[Dep] = Some(defaultVersions(crossValue)("chisel-plugin"))
//  override def scalaVersion = Agg(defaultVersions(crossValue)("scala")).toString
  override def scalaVersion = scalaVersions(crossValue)
  override def scalacOptions = super.scalacOptions() ++
    Agg("-Ymacro-annotations", "-Ytasty-reader") ++ Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
  )
  override def ivyDeps = super.ivyDeps() ++ Agg(chiselIvy.get) ++ Agg(chiselPluginIvy.get)
  override def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(chiselPluginIvy.get)
}

object fudian extends Cross[FuDian]("chisel", "chisel3")
trait FuDian extends Cross.Module[String] with HasChisel {
  override def millSourcePath = os.pwd / "fudian"
}

trait FPUv2Module extends Cross.Module[String] with ScalaModule {
  def fudianModule: ScalaModule
  override def moduleDeps = super.moduleDeps ++ Seq(
    fudianModule,
  )
}

object fpuv2 extends Cross[FPUv2]("chisel", "chisel3")
trait FPUv2 extends FPUv2Module with HasChisel {
  override def millSourcePath = os.pwd

  def fudianModule: FuDian = fudian(crossValue)
  override def forkArgs = Seq("-Xmx40G", "-Xss256m")
  override def ivyDeps = super.ivyDeps() ++ Agg(
    defaultVersions(crossValue)("chiseltest")
  )

  object test extends SbtModuleTests with TestModule.ScalaTest {
    override def forkArgs = Seq("-Xmx40G", "-Xss256m")
    override def ivyDeps = super.ivyDeps() ++ Agg(
      defaultVersions(crossValue)("chiseltest")
    )
  }
}

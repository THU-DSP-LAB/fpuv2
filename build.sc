import mill._
import scalalib._
import scalafmt._

object ivys {
  val sv = "2.13.10"

  val chisel = ivy"org.chipsalliance::chisel:6.4.0"
  val chiselPlugin = ivy"org.chipsalliance:::chisel-plugin:6.4.0"
  val chiseltest = ivy"edu.berkeley.cs::chiseltest:5.0.2"
}

object fudian extends ScalaModule {
  override def millSourcePath = os.pwd / "fudian"
  override def scalaVersion = ivys.sv

  override def ivyDeps = Agg(
    ivys.chisel,
    ivys.chiseltest
  )
}

object fpuv2 extends SbtModule with ScalaModule with ScalafmtModule {
  override def millSourcePath = millOuterCtx.millSourcePath
  override def scalaVersion = ivys.sv

  override def ivyDeps = Agg(
    ivys.chisel,
    ivys.chiseltest,
  )

  override def scalacPluginIvyDeps = Agg(
    ivys.chiselPlugin
  )

  override def scalacOptions = Seq("-Xsource:2.13.10")

  override def moduleDeps = Seq(fudian)

  object test extends SbtModuleTests with TestModule.ScalaTest {

    override def ivyDeps = super.ivyDeps() ++ Agg(
      ivys.chiseltest,
    )
  }
}

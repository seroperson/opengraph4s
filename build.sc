import mill._
import mill.scalalib._
import mill.scalalib.publish._

object Version {
  val scala212 = "2.12.18"
  val scala213 = "2.13.12"
  val scala336 = "3.3.6"
  val scalaCross = Seq(scala212, scala213, scala336)

  val cats = "2.10.0"
  val catsEffect = "3.5.1"

  val sttpClient = "4.0.0-M5"

  val jsoup = "3.1.0"

  val scalaTest = "3.2.16"
  val scalaTestEffects = "1.5.0"

  val zio = "2.0.16"
  val zioInteropCats = "23.1.0.0"
  val pprint = "0.7.0"
}

object Library {
  val catsCore = ivy"org.typelevel::cats-core::${Version.cats}"
  val catsEffectStd = ivy"org.typelevel::cats-effect-std::${Version.catsEffect}"

  val sttpClient =
    ivy"com.softwaremill.sttp.client4::core::${Version.sttpClient}"
  val sttpClientFs2 =
    ivy"com.softwaremill.sttp.client4::fs2::${Version.sttpClient}"

  val jsoup = ivy"net.ruippeixotog::scala-scraper::${Version.jsoup}"

  // tests
  val scalaTest = ivy"org.scalatest::scalatest::${Version.scalaTest}"
  val scalaTestEffects =
    ivy"org.typelevel::cats-effect-testing-scalatest::${Version.scalaTestEffects}"

  // examples
  val pprint = ivy"com.lihaoyi::pprint:${Version.pprint}"

  val zio = ivy"dev.zio::zio::${Version.zio}"
  val zioInteropCats = ivy"dev.zio::zio-interop-cats::${Version.zioInteropCats}"

  val catsEffect = ivy"org.typelevel::cats-effect::${Version.catsEffect}"
}

trait BaseModule extends CrossScalaModule {

  override def scalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Xlint:_",
    "-Wconf:cat=lint-byname-implicit:s",
    "-Ywarn-dead-code"
  )

}

object opengraph4s extends Cross[OpenGraph4sModule](Version.scalaCross)
trait OpenGraph4sModule extends BaseModule with PublishModule {

  override def publishVersion: T[String] =
    "0.1.0"

  override def pomSettings = PomSettings(
    description =
      "Deadly-simple library for fetching OpenGraph tags from a given webpage.",
    organization = "me.seroperson",
    url = "https://github.com/seroperson/opengraph4s",
    licenses = Seq(License.Common.MIT),
    versionControl = VersionControl.github("seroperson", "opengraph4s"),
    developers = Seq(
      Developer("seroperson", "Daniil Sivak", "https://seroperson.me/")
    )
  )

  override def ivyDeps = T {
    super.ivyDeps() ++ Agg(
      Library.catsCore,
      Library.catsEffectStd,
      Library.sttpClient,
      Library.sttpClientFs2,
      Library.jsoup
    )
  }

  object test extends ScalaTests with TestModule.ScalaTest {

    override def ivyDeps = T {
      super.ivyDeps() ++ Agg(
        Library.scalaTest,
        Library.scalaTestEffects
      )
    }
  }
}

object example extends Module {

  object zio extends Cross[ZioModule](Version.scalaCross)
  trait ZioModule extends BaseModule {

    override def ivyDeps = T {
      super.ivyDeps() ++ Agg(
        Library.pprint,
        Library.zio,
        Library.zioInteropCats
      )
    }

    override def moduleDeps =
      super.moduleDeps ++ Seq(opengraph4s())
  }

  object cats extends Cross[CatsModule](Version.scalaCross)
  trait CatsModule extends BaseModule {

    override def ivyDeps = T {
      super.ivyDeps() ++ Agg(
        Library.pprint,
        Library.catsEffect
      )
    }

    override def moduleDeps =
      super.moduleDeps ++ Seq(opengraph4s())
  }

}

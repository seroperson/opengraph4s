package me.seroperson.opengraph4s

import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import me.seroperson.opengraph4s.client.{Client, SttpClient}
import me.seroperson.opengraph4s.model.ParseData
import me.seroperson.opengraph4s.parser.{Parser, ScalaScrapperParser}
import net.ruippeixotog.scalascraper.browser.HtmlUnitBrowser
import sttp.client4.httpclient.fs2.HttpClientFs2Backend

object ExampleCatsApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val parser: Parser[ParseData, IO] =
      ScalaScrapperParser[IO](() => HtmlUnitBrowser())
    val backend = HttpClientFs2Backend.resource[IO](
      customizeRequest = { request =>
        java.net.http.HttpRequest
          // requires jdk16+
          .newBuilder(
            request,
            { (name, value) =>
              false
            }
          )
          .header("Accept-Language", "en-US,en;q=0.9")
          .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36"
          )
          .build()
      }
    )
    val clientResource: Resource[IO, Client[IO]] = SttpClient(
      parser,
      backend,
      fetchFullPage = false
    )

    clientResource.use { client =>
      for {
        url <- IO.fromOption(args.headOption) {
          new IllegalStateException("You have to pass at least 1 URL to fetch")
        }
        og <- client.request(url)
        _ <- cats.effect.std.Console[IO].println(pprint.apply(og))
      } yield {
        ExitCode.Success
      }
    }
  }

}

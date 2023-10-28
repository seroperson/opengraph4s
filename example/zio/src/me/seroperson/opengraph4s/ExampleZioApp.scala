package me.seroperson.opengraph4s

import me.seroperson.opengraph4s.client.{SttpClient}
import me.seroperson.opengraph4s.model.ParseData
import me.seroperson.opengraph4s.parser.{Parser, ScalaScrapperParser}
import sttp.client4.httpclient.fs2.HttpClientFs2Backend
import zio.{Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault}

object ExampleZioApp extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    import zio.interop.catz._

    val parser: Parser[ParseData, Task] = ScalaScrapperParser[Task]()
    val backend = HttpClientFs2Backend.resource[Task](
      customizeRequest = { request =>
        java.net.http.HttpRequest
          // requires jdk16+
          .newBuilder(request, { (name, value) =>
            false
          })
          .header("Accept-Language", "en-US,en;q=0.9")
          .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
          .build()
      }
    )

    for {
      args <- getArgs
      url <- ZIO.fromOption(args.headOption)
        .mapError { _ =>
          new IllegalStateException("You have to pass at least 1 URL to fetch")
        }
      client <- SttpClient(parser, backend)
        .toScopedZIO
      og <- client.request(url)
      status <- zio.Console.printLine(pprint.apply(og))
        .exit
    } yield {
      status
    }
  }

}

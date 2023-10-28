package me.seroperson.opengraph4s.client

import cats.MonadThrow
import cats.effect.kernel.{Async, Resource}
import cats.implicits._
import me.seroperson.opengraph4s.model.{OpenGraphParsed, ParseData}
import me.seroperson.opengraph4s.parser.Parser
import sttp.capabilities.fs2.Fs2Streams
import sttp.client4.{StreamBackend, asStreamUnsafe, basicRequest}
import sttp.model.Uri

import java.nio.charset.StandardCharsets

/**
 * Client implementation using `sttp` library.
 *
 * @param parser parser which will be used to parse a webpage.
 * @param backend `sttp` backend resource instance.
 * @param bufferSize a webpage will be fetched using buffered stream with the
 *                   given size.
 * @param fetchFullPage whether to fetch or not a full page. If `false`
 *                      (default), then a webpage will be fetched until
 *                      `</head>` bytes sequence appeared. Otherwise, the full
 *                      page will be fetched.
 *
 * @note Sometimes not-fully-fetched webpage can lead to errors if js-enabled
 *       [[parser]] is used. So, be sure to construct a client which fits your
 *       needs.
 * */
class SttpClient[F[_] : Async : MonadThrow](
  parser: Parser[ParseData, F],
  backend: StreamBackend[F, Fs2Streams[F]],
  bufferSize: Int = 512,
  fetchFullPage: Boolean = false
) extends Client[F] {

  override def request(
    url: String
  ): F[OpenGraphParsed] = {
    for {
      uri <- MonadThrow[F]
        .fromEither(
          Uri
            .parse(url)
            .leftMap(ex => new Throwable(ex))
        )

      request = basicRequest
        .get(uri)
        .followRedirects(true)
        .response(asStreamUnsafe(Fs2Streams[F]))

      response <- request.send(backend)

      stream <- MonadThrow[F]
        .fromEither(
          response
            .body
            .leftMap(ex => new Throwable(ex))
        )

      result <- stream
        .buffer(bufferSize)
        .chunkMin(bufferSize)
        .takeThrough { chunk =>
          // todo: some other way to find </head>?
          fetchFullPage || !chunk.toList.containsSlice(SttpClient.HeadEnclosingAsBytes)
        }
        .foldMonoid
        .map(_.toArray)
        .evalMap { bytes =>
          // todo: get charset from response?
          parser.parse(ParseData(bytes, StandardCharsets.UTF_8))
        }
        .compile
        .lastOrError
    } yield result
  }

}

object SttpClient {

  // todo: respect non-utf-8 charset
  private val HeadEnclosingAsBytes = "</head>".getBytes(StandardCharsets.UTF_8)

  def apply[F[_] : Async : MonadThrow](
    parser: Parser[ParseData, F],
    backend: Resource[F, StreamBackend[F, Fs2Streams[F]]],
    bufferSize: Int = 512,
    fetchFullPage: Boolean = false
  ): Resource[F, SttpClient[F]] = {
    for {
      b <- backend
    } yield {
      new SttpClient[F](parser, b, bufferSize, fetchFullPage)
    }
  }

}
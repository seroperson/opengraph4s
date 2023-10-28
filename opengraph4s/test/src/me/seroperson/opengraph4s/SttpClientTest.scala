package me.seroperson.opengraph4s

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.Stream
import me.seroperson.opengraph4s.client.SttpClient
import me.seroperson.opengraph4s.parser.ScalaScrapperParser
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import sttp.capabilities.fs2.Fs2Streams
import sttp.client4.impl.cats.implicits.asyncMonadError
import sttp.client4.testing.WebSocketStreamBackendStub
import sttp.client4.{Response, StreamBackend}
import sttp.monad.MonadError

import java.nio.charset.StandardCharsets

class SttpClientTest extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  val twitchTv: String =
    """
      |<!DOCTYPE html>
      |<html class="tw-root--hover">
      |   <head>
      |      <meta charset="utf-8">
      |      <title>Twitch</title>
      |      <meta name="twitter:app:id:ipad" content="id460177396"/>
      |      <meta name="twitter:app:url:ipad" content="twitch.tv"/>
      |      <meta content="Twitch" name="title"/>
      |      <meta name="twitter:title" content="Twitch"/>
      |      <meta name="twitter:app:name:googleplay" content="Twitch"/>
      |      <meta property="og:site_name" content="Twitch"/>
      |      <meta name="twitter:app:id:iphone" content="id460177396"/>
      |      <meta name="twitter:app:name:ipad" content="Twitch"/>
      |      <meta name="robots" content="index"/>
      |      <meta name="twitter:description" content="Twitch is an interactive livestreaming service for content spanning gaming, entertainment, sports, music, and more. There’s something for everyone on Twitch."/>
      |      <meta name="twitter:app:url:iphone" content="twitch.tv"/>
      |      <meta name="twitter:site" content="@twitch"/>
      |      <meta property="al:ios:app_store_id" content="id460177396"/>
      |      <meta name="twitter:app:id:googleplay" content="tv.twitch.android.app"/>
      |      <meta name="twitter:app:url:googleplay" content="twitch.tv"/>
      |      <meta property="al:android:app_name" content="Twitch"/>
      |      <meta name="twitter:app:name:iphone" content="Twitch"/>
      |      <meta content="https://www.twitch.tv" property="og:url"/>
      |      <meta property="og:title" content="Twitch"/>
      |      <meta property="al:android:package" content="tv.twitch.android.app"/>
      |      <meta name="twitter:image" content="https://static-cdn.jtvnw.net/ttv-static-metadata/twitch_logo3.jpg"/>
      |      <meta property="fb:app_id" content="161273083968709"/>
      |      <meta property="al:ios:url" content="twitch.tv"/>
      |      <link rel="alternate" hreflang="zh-tw" href="https://www.twitch.tv/?lang=zh-tw"/>
      |      <link rel="alternate" hreflang="x-default" href="https://www.twitch.tv"/>
      |      <script type="application/ld+json">[{"@id":"https://www.twitch.tv/","@type":"Organization","name":"Twitch","sameAs":["https://www.facebook.com/Twitch/","https://www.linkedin.com/company/twitch-tv/","https://en.wikipedia.org/wiki/Twitch.tv","https://twitter.com/twitch"],"url":"https://www.twitch.tv/"}]</script>
      |      <meta content="Twitch is an interactive livestreaming service for content spanning gaming, entertainment, sports, music, and more. There’s something for everyone on Twitch." name="description"/>
      |      <meta property="og:type" content="website"/>
      |      <meta content="https://static-cdn.jtvnw.net/ttv-static-metadata/twitch_logo3.jpg" property="og:image"/>
      |      <meta property="og:description" content="Twitch is an interactive livestreaming service for content spanning gaming, entertainment, sports, music, and more. There’s something for everyone on Twitch."/>
      |      <meta name="twitter:card" content="summary"/>
      |      <meta property="al:ios:app_name" content="Twitch"/>
      |   </head>
      |   <somerandomtag class="
      |""".stripMargin

  val client = SttpClient[IO](
    ScalaScrapperParser[IO](),
    Resource.make(
      IO
        .pure(
          new WebSocketStreamBackendStub[IO, Fs2Streams[IO]](
            MonadError[IO],
            {
              case _ /*@ Request(
                GET,
                Uri(
                  scheme,
                  authority,
                  pathSegments,
                  querySegments,
                  fragmentSegment
                ),
                body,
                headers,
                response,
                options,
                tags
              )*/ =>
                IO.pure(
                  Response.ok(
                    Right(
                      Stream[IO, Byte](
                        twitchTv.getBytes(StandardCharsets.UTF_8):_*
                      )
                    )
                  )
                )
            },
            None
          ): StreamBackend[IO, Fs2Streams[IO]]
        )
    )(_.close())
  )

  "correctly parse real-life case" in {
    client
      .use {
        _
          .request("https://twitch.tv/")
          .asserting { response =>
            (
              response.title,
              response.url,
              response.image,
              response.`type`,
              response.siteName,
              response.description,
              response.metaProperties.get("al:android:app_name")
            ) shouldBe(
              Some("Twitch"),
              Some("https://www.twitch.tv"),
              Some("https://static-cdn.jtvnw.net/ttv-static-metadata/twitch_logo3.jpg"),
              Some("website"),
              Some("Twitch"),
              Some("Twitch is an interactive livestreaming service for content spanning gaming, entertainment, sports, music, and more. There’s something for everyone on Twitch."),
              Some("Twitch")
            )
          }
      }
  }

}

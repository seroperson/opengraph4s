package me.seroperson.opengraph4s

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import me.seroperson.opengraph4s.model.ParseData
import me.seroperson.opengraph4s.parser.{Parser, ScalaScrapperParser}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets

class ScalaScrapperParserTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers {

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

  val titleProperty: String =
    """
      |<head>
      |    <meta content="Title" property="title"/>
      |</head>
      |""".stripMargin

  val titleName: String =
    """
      |<head>
      |    <meta content="Title" name="title"/>
      |</head>
      |""".stripMargin

  val titleInPropertyAndName: String =
    """
      |<head>
      |    <meta content="TitleN" name="title"/>
      |    <meta content="TitleP" property="title"/>
      |</head>
      |""".stripMargin

  val titleInOgAndProperty: String =
    """
      |<head>
      |    <meta content="TitleOG" property="og:title"/>
      |    <meta content="TitleP" property="title"/>
      |</head>
      |""".stripMargin

  val titleTag: String =
    """
      |<head>
      |   <title>Title</title>
      |</head>
      |""".stripMargin

  val urlOgProperty: String =
    """
      |<head>
      |    <meta content="url" property="og:url"/>
      |</head>
      |""".stripMargin

  val imageOgProperty: String =
    """
      |<head>
      |    <meta content="image" property="og:image"/>
      |</head>
      |""".stripMargin

  val typeOgProperty: String =
    """
      |<head>
      |    <meta content="type" property="og:type"/>
      |</head>
      |""".stripMargin

  val siteNameOgProperty: String =
    """
      |<head>
      |    <meta content="site_name" property="og:site_name"/>
      |</head>
      |""".stripMargin

  val descriptionInOgAndProperty: String =
    """
      |<head>
      |    <meta content="DescriptionOG" property="og:description"/>
      |    <meta content="DescriptionP" property="description"/>
      |</head>
      |""".stripMargin

  val descriptionProperty: String =
    """
      |<head>
      |    <meta content="DescriptionP" property="description"/>
      |</head>
      |""".stripMargin

  val parser: Parser[ParseData, IO] = ScalaScrapperParser[IO]()

  "parse title from meta with property attr" in {
    parser
      .parse(
        ParseData(
          titleProperty.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.title shouldBe Some("Title")
      }
  }

  "parse title from meta with name attr" in {
    parser
      .parse(
        ParseData(
          titleName.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.title shouldBe Some("Title")
      }
  }

  "prefer title from property rather than from name attr" in {
    parser
      .parse(
        ParseData(
          titleInPropertyAndName.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.title shouldBe Some("TitleP")
      }
  }

  "prefer title from og:title rather than from title" in {
    parser
      .parse(
        ParseData(
          titleInOgAndProperty.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.title shouldBe Some("TitleOG")
      }
  }

  "parse title from tag" in {
    parser
      .parse(
        ParseData(
          titleTag.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.title shouldBe Some("Title")
      }
  }

  "parse url from og:url" in {
    parser
      .parse(
        ParseData(
          urlOgProperty.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.url shouldBe Some("url")
      }
  }

  "parse image from og:image" in {
    parser
      .parse(
        ParseData(
          imageOgProperty.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.image shouldBe Some("image")
      }
  }

  "parse type from og:type" in {
    parser
      .parse(
        ParseData(
          typeOgProperty.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.`type` shouldBe Some("type")
      }
  }

  "parse site_name from og:site_name" in {
    parser
      .parse(
        ParseData(
          siteNameOgProperty.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.siteName shouldBe Some("site_name")
      }
  }

  "parse description from description" in {
    parser
      .parse(
        ParseData(
          descriptionProperty.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.description shouldBe Some("DescriptionP")
      }
  }

  "prefer description from og:description rather than from description" in {
    parser
      .parse(
        ParseData(
          descriptionInOgAndProperty.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        response.description shouldBe Some("DescriptionOG")
      }
  }

  "correctly parse real-life case" in {
    parser
      .parse(
        ParseData(
          twitchTv.getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
        )
      )
      .asserting { response =>
        (
          response.title,
          response.url,
          response.image,
          response.`type`,
          response.siteName,
          response.description,
          response.metaProperties.get("al:android:app_name")
        ) shouldBe (
          Some("Twitch"),
          Some("https://www.twitch.tv"),
          Some(
            "https://static-cdn.jtvnw.net/ttv-static-metadata/twitch_logo3.jpg"
          ),
          Some("website"),
          Some("Twitch"),
          Some(
            "Twitch is an interactive livestreaming service for content spanning gaming, entertainment, sports, music, and more. There’s something for everyone on Twitch."
          ),
          Some("Twitch")
        )
      }
  }

}

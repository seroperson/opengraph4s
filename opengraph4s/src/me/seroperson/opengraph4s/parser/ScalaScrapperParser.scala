package me.seroperson.opengraph4s.parser

import cats.MonadThrow
import cats.implicits._
import me.seroperson.opengraph4s.model.{OpenGraphParsed, ParseData}
import net.ruippeixotog.scalascraper.browser.{
  Browser,
  HtmlUnitBrowser,
  JsoupBrowser
}
import net.ruippeixotog.scalascraper.dsl.DSL.deepFunctorOps

/** Parser implementation using `scala-scrapper` library.
  *
  * @param browser
  *   function which returns a [[Browser]] object which will be used to parse
  *   the given webpage. Can be either [[JsoupBrowser]] (without js support) or
  *   [[HtmlUnitBrowser]] (with js support). Take in mind that non-full webpage
  *   can be passed to [[parse()]], so using [[HtmlUnitBrowser]] can lead to
  *   errors during the parsing.
  */
class ScalaScrapperParser[F[_]: MonadThrow](
    browser: () => Browser
) extends Parser[ParseData, F] {

  override def parse(data: ParseData): F[OpenGraphParsed] = {
    MonadThrow[F]
      .pure(browser())
      .map { browser =>
        val html = browser
          .parseString(new String(data.bytes, data.charset))
        val metaElements = html.head >> (_.select("meta"))
        val titleTagFallback = html.title

        OpenGraphParsed(
          metaProperties = metaElements.collect {
            case element
                if element.hasAttr("content") && (element.hasAttr("property") ||
                  element.hasAttr("name")) =>
              (
                if (element.hasAttr("property")) {
                  element.attr("property")
                } else {
                  element.attr("name")
                },
                element.attr("content")
              )
          }.toMap,
          titleTagFallback = if (titleTagFallback.isEmpty) {
            None
          } else {
            Some(titleTagFallback)
          }
        )
      }
  }

}

object ScalaScrapperParser {

  sealed trait ScalaScrapperBrowserType
  case object JsoupBrowserType extends ScalaScrapperBrowserType
  case object HtmlUnitBrowserType extends ScalaScrapperBrowserType

  /** Creates an object which uses [[JsoupBrowser]] to parse the webpage.
    */
  def apply[F[_]: MonadThrow](): ScalaScrapperParser[F] =
    new ScalaScrapperParser(() => JsoupBrowser())

  /** Creates an object which uses [[JsoupBrowser]] or [[HtmlUnitBrowser]]
    * browser to parse the webpage.
    */
  def apply[F[_]: MonadThrow](
      t: ScalaScrapperBrowserType
  ): ScalaScrapperParser[F] =
    new ScalaScrapperParser(() =>
      t match {
        case JsoupBrowserType    => JsoupBrowser()
        case HtmlUnitBrowserType => HtmlUnitBrowser()
      }
    )

  /** Creates an object which uses the given browser to parse the webpage.
    */
  def apply[F[_]: MonadThrow](browser: () => Browser): ScalaScrapperParser[F] =
    new ScalaScrapperParser(browser)

}

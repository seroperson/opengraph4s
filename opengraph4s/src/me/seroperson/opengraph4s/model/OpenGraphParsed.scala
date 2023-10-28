package me.seroperson.opengraph4s.model

/**
 * Class represents parsed `head` element of a page.
 *
 * @param metaProperties contains all the meta tags parsed from a page.
 *                       Key is a value of `property` or `name` attribute,
 *                       value is a value of `content` attribute.
 * @param titleTagFallback text of `title` tag if any.
 * */
case class OpenGraphParsed(
  metaProperties: Map[String, String],
  titleTagFallback: Option[String]
) {
  /**
   * Holds a `og:title` or `title` value from meta tags.
   * Or value from `title` tag otherwise.
   * */
  lazy val title = metaProperties.get("og:title")
    .orElse(metaProperties.get("title"))
    .orElse(titleTagFallback)
  /**
   * Holds a `og:url` value from meta tag.
   * */
  lazy val url = metaProperties.get("og:url")
  /**
   * Holds a `og:image` value from meta tag.
   * */
  lazy val image = metaProperties.get("og:image")
  /**
   * Holds a `og:type` value from meta tag.
   * */
  lazy val `type` = metaProperties.get("og:type")
  /**
   * Holds a `og:site_name` value from meta tag.
   * */
  lazy val siteName = metaProperties.get("og:site_name")
  /**
   * Holds a `og:description` or `description` value from meta tag.
   * */
  lazy val description = metaProperties.get("og:description")
    .orElse(metaProperties.get("description"))
}

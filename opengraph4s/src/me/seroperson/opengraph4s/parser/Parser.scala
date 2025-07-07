package me.seroperson.opengraph4s.parser

import me.seroperson.opengraph4s.model.OpenGraphParsed

/** Represents the base class for parsers.
  *
  * Parser is responsible for parsing a `head` element and constructing a
  * [[OpenGraphParsed]] object.
  *
  * @see
  *   [[ScalaScrapperParser]] implementation using `scala-scrapper` library.
  *
  * @tparam C
  *   represents the type which this parser is able to parse.
  * @tparam F
  *   effect type.
  */
trait Parser[C, F[_]] {

  /** Parses the given data and fills an [[OpenGraphParsed]] object.
    *
    * @param data
    *   data to parse. It is not guaranteed that it represents a fully fetched
    *   webpage.
    *
    * @return
    *   parsed data.
    */
  def parse(data: C): F[OpenGraphParsed]

}

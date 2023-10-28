package me.seroperson.opengraph4s.client

import me.seroperson.opengraph4s.model.OpenGraphParsed

/**
 * The main abstraction which is responsible for fetching data and returning
 * parsed OpenGraph object as a result.
 *
 * @see [[SttpClient]] implementation using `sttp` library to fetch a webpage.
 *
 * @tparam F effect type.
 * */
trait Client[F[_]] {

  /**
   * Requests the given URL, parses head element and returns parsed element
   * as [[OpenGraphParsed]] object.
   *
   * @param url the URL to request.
   *
   * @return parsed OpenGraph data.
   */
  def request(url: String): F[OpenGraphParsed]

}

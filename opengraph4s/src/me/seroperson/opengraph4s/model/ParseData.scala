package me.seroperson.opengraph4s.model

import java.nio.charset.Charset

/** Represents data to parse.
  *
  * @param bytes
  *   array of bytes which (usually) contains full `<head>` element (but not the
  *   whole page).
  * @param charset
  *   webpage's charset.
  */
case class ParseData(
    bytes: Array[Byte],
    charset: Charset
)

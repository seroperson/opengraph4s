# opengraph4s

Deadly-simple library for fetching OpenGraph tags from a given webpage.

## Installation

In case if you use `sbt`:

```sbt
libraryDependencies += "me.seroperson" %% "opengraph4s" % "0.1.0"
```

In case of `mill`:

```scala
ivy"me.seroperson::opengraph4s::0.1.0"
```

## How to use

Default implementation uses `sttp`, `fs2` and `scala-scrapper` to fetch and parse tags.
Shortly, to get started, it is enough to code something like this:

```scala
import cats.effect.IO
import sttp.client4.httpclient.fs2.HttpClientFs2Backend

import me.seroperson.opengraph4s.client.SttpClient
import me.seroperson.opengraph4s.parser.ScalaScrapperParser

val parser = ScalaScrapperParser[IO]()
val backend = HttpClientFs2Backend.resource[IO]()
val client = SttpClient(parser, backend)

for {
  og <- client.request("https://example.com/")
  _ <- cats.effect.std.Console[IO].println(og)
} yield og
```

It initializes `parser`, which is responsible for parsing a fetched webpage, and `client`,
which is responsible for fetching the webpage itself. The function
`Client#request(String)` fetches a webpage and returns `F[OpenGraphParsed]` with all the
parsed information.

You probably want to override headers on your requests to a webpage, such as
`Accept-Language`, `User-Agent` and others, as long as many webpages response
differently depending on user configuration. While using default implementation it is
configurable easily by passing customized `backend` parameter to `SttpClient` (see
examples).

## Examples

This repository contains `example/zio` and `example/cats` projects. You can run them like
this:

```sh
./millw example.__.run https://twitch.tv
```

Here `example/cats` uses `HtmlUnitBrowser` as parser and `IO` as effect type, while
`example/zio` uses `JsoupBrowser` and `Task`.

## Caveats

Default implementation works quite fast and parses simple pages correctly, but when it
comes to non-plain-html websites (like SPAs), `<head>` tag can be generated dynamically
then, which results in empty `OpenGraphParsed` response. Luckily, most of the cases can
be solved by using `HttpUnitBrowser` (from `scala-scrapper` library) with
`ScalaScrapperParser#apply`. It renders JS and generates a html page for you, but take
in mind that usually it works slower than default `JsoupBrowser` (because it renders JS
actually) and it is not guaranteed that JS will be executed correctly.

Also, a webpage can have any kind of bot-protection: captcha, 403 for headless browser
and so on.

This library doesn't handle such cases. If you are looking for any guarantees, probably
it is better to consider using some SaaS web-scrappers then.

## License

```text
MIT License

Copyright (c) 2023 Daniil Sivak

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

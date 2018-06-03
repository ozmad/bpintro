package ozma.routes

import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.headers._

object BaseRoute {

  val contentTypeJson = `Content-Type`(`application/json`)

  val contentTypeJsonHeader = RawHeader("Content-Type", "application/json")
}

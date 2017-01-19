package controllers

import play.api.libs.ws.WS
import play.api.mvc.{Action, Controller, Result}
import play.mvc.Http.Response

import scala.concurrent.Future

/**
  * Created by ottenmac1 on 12/16/16.
@Singleton
class ProxyController extends Controller {

  def proxy = Action.async {
    val responseFuture: Future[Response] = WS.url("http://example.com").get()

    val resultFuture: Future[Result] = responseFuture.map { resp =>
      // Create a Result that uses the http status, body, and content-type
      // from the example.com Response
      Status(resp.status)(resp.body).as(resp.ahcResponse.getContentType)
    }

    Async(resultFuture)

    // How do we create a Result from a Future[Response]?
  }

}
  */

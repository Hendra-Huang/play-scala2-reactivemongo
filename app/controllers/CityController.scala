package controllers

import javax.inject._

import models.City
import models.daos.CityDao
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json.collection.JSONCollection
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CityController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  def citiesFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("city"))

  def create(name: String, population: Int) = Action.async {
    for {
      cities <- citiesFuture
      lastError <- CityDao.save(City(name, population))
    } yield
      Ok("Mongo LastError: %s".format(lastError))
  }

  def createFromJson = Action.async(parse.json) { request =>
    Json.fromJson[City](request.body) match {
      case JsSuccess(city, _) =>
        for {
          cities <- citiesFuture
          lastError <- CityDao.save(city)
        } yield {
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created("Created 1 city")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a city from the json provided. " + Errors.show(errors)))
    }
  }

  def createBulkFromJson = Action.async(parse.json) { request =>
    Json.fromJson[Seq[City]](request.body) match {
      case JsSuccess(newCities, _) =>
        citiesFuture.flatMap { cities =>
          val documents = newCities.map(implicitly[cities.ImplicitlyDocumentProducer](_))

          cities.bulkInsert(ordered = true)(documents: _*).map { multiResult =>
            Logger.debug(s"Successfully inserted with multiResult: $multiResult")
            Created(s"Created ${multiResult.n} cities")
          }
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a city from the json provided. " + Errors.show(errors)))
    }
  }

  def findByName(name: String) = Action.async {
//    CityDao.findByName(name) map { cities =>
    import reactivemongo.bson.BSONObjectID
    CityDao.find(BSONObjectID.parse(name).get) map { cities =>
      Ok(Json.toJson(cities))
    }
  }
}

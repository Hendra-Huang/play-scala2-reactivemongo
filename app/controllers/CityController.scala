package controllers

import javax.inject._

import models.City
import models.daos.CityDao
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CityController extends Controller {

  def create(name: String, population: Int) = Action.async {
    for {
      lastError <- CityDao.save(City(name, population))
    } yield
      Ok("Mongo LastError: %s".format(lastError))
  }

  def createFromJson = Action.async(parse.json) { request =>
    Json.fromJson[City](request.body) match {
      case JsSuccess(city, _) =>
        for {
          lastError <- CityDao.save(city)
        } yield {
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created("Created 1 city")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a city from the json provided. " + errors))
    }
  }

  def findByName(name: String) = Action.async {
    CityDao.findByName(name) map { cities =>
      Ok(Json.toJson(cities))
    }
  }
}

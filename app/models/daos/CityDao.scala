package models.daos

import models.City
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CityDao extends MongoDao {
  override val collectionName: String = "city"

  def findByName(name: String)(implicit format: Format[City]): Future[List[City]] = {
    collection.find(Json.obj("name" -> name)).
      sort(Json.obj("created" -> -1)).
      cursor[City].collect[List]()
  }

  def find(id: BSONObjectID)(implicit format: Format[City]): Future[Option[City]] = {
    super._find[City](id)
  }

  def save(city: City)(implicit format: Format[City]): Future[City] = {
//    super._save[City](city)
        collection.insert[City](city)
        Future.successful(city)
  }
}

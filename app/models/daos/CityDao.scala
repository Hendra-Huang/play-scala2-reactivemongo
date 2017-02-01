package models.daos

import models.City
import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CityDao extends MongoDao {
  override val collectionName: String = "city"

  def find(id: BSONObjectID): Future[Option[City]] = {
    super.findById[City](id)
  }

  def save(model: City): Future[WriteResult] = {
    super.save[City](model)
  }

  def findByName(name: String)(implicit format: OFormat[City]): Future[List[City]] = {
    collection.flatMap(_.find(Json.obj("name" -> name))
      .sort(Json.obj("created" -> -1))
      .cursor[City]()
      .collect[List]()
    )
  }
}

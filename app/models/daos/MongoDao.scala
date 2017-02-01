package models.daos

import com.typesafe.config.ConfigFactory
import play.api.libs.json._
import reactivemongo.api._
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MongoDao {
  private val config = ConfigFactory.load
  private val driver = new MongoDriver

  protected def connection: MongoConnection = driver.connection(List(config.getString("mongodb.host")))

  protected def default: Future[DB] = connection.database(config.getString("mongodb.db"))

  protected val collectionName: String

  protected def collection: Future[JSONCollection] = default.map(_.collection[JSONCollection](collectionName))

  protected def findById[Model](id: BSONObjectID)(implicit format: OFormat[Model]): Future[Option[Model]] = {
    collection.flatMap(_.find(Json.obj("_id" -> id)).one[Model])
  }

  protected def save[Model](model: Model)(implicit format: OFormat[Model]): Future[WriteResult] = {
    collection.flatMap(_.insert[Model](model))
  }
}

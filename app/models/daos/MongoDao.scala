package models.daos

import com.typesafe.config.ConfigFactory
import play.api.libs.json._
import reactivemongo.api._
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.play.json.collection._

trait MongoDao {
  private val config = ConfigFactory.load
  private val driver = new MongoDriver

  protected def connection: MongoConnection = driver.connection(List(config.getString("mongodb.host")))

  protected def default: DB = connection(config.getString("mongodb.db"))

  protected val collectionName: String

  protected def collection: JSONCollection = default.collection[JSONCollection](collectionName)

  protected def _find[T](id: BSONObjectID)(implicit format: Format[T]): Future[Option[T]] = {
    collection.find(Json.obj("_id" -> id)).one[T]
  }

  protected def _save[T](model: T)(implicit format: Format[T]): Future[T] = {
    collection.insert[T](model)
    Future.successful(model)
  }
}

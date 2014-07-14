package ca.pgx.common.db.helpers

import com.mongodb.{BasicDBList, DBObject}
import net.liftweb.common._
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.field.MongoListField

import scala.reflect.Manifest

/**
 * A field that contains a list of enums of the same type. It can properly serialize and
 * deserialize enums to String representation in MongoDB.
 */
class MongoEnumListField[OwnerType <: BsonRecord[OwnerType], EnumType <: Enumeration]
(rec: OwnerType, protected val enum: EnumType)(implicit m: Manifest[EnumType#Value])
  extends MongoListField[OwnerType, EnumType#Value](rec) {

  import scala.collection.JavaConversions._

  override def setFromDBObject(dbo: DBObject): Box[MyType] = {
    val convertedResults = dbo.asInstanceOf[BasicDBList].toList.map {
      case s: String => enum.values.find(_.toString == s)
      case _ => None
    }
    if (convertedResults.contains(None))
      setBox(Failure("Error parsing database value into an Enumeration."))
    else
      setBox(Full(convertedResults.flatten))
  }
}

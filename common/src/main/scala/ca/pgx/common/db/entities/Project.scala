package ca.pgx.common.db.entities

import ca.pgx.common.db.collections.CollectionNames
import ca.pgx.common.db.helpers.InjectableMetaRecord
import ca.pgx.common.events.EventAction
import com.foursquare.index.IndexedRecord
import com.mongodb.{BasicDBList, DBObject}
import net.liftweb.common._
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.{MongoListField, ObjectIdRefListField, DateField, ObjectIdPk}
import net.liftweb.record.field.{LongField, StringField}

/**
 * A generic project configuration entity. For event hub to be able to process events
 * they should belong to a project.
 */
class Project extends MongoRecord[Project] with ObjectIdPk[Project] with IndexedRecord[Project] {

  override def meta = Project

  // TODO: enforce uniqueness via index or in the code
  /**
   * Name of a project. This has to be unique for all projects because that's how
   * project configuration is looked up.
   */
  object name extends StringField(this, 256)

  /**
   * Start and end dates of a project define when EventHub will accept requests for a project.
   * After project is no longer active it's better to deactivate it by setting endDate.
   * This will allow historical information to be coherent, but all related info can be
   * deleted as well as the project if desired.
   */
  object startDate extends DateField(this)

  /**
   * By default this will not be set, so the project is active indeterminately.
   */
  object endDate extends DateField(this)

  /**
   * Users/clients who are allowed to write/submit results to this project.
   */
  object writers extends ObjectIdRefListField(this, User)

  /**
   * Users/clients who are allowed to read results from this project.
   */
  object readers extends ObjectIdRefListField(this, User)

  /**
   * Interval in seconds during which the client is expected to submit a reading
   * If the reading is not submitted within this interval then the alert action will be raised
   */
  object submitIntervalSec extends LongField(this)

  /**
   * List of event actions to take place on late submit
   */
  object lateSubmitAlerts extends MongoListField[Project, EventAction.Value](this) {
    import scala.collection.JavaConversions._

    override def setFromDBObject(dbo: DBObject): Box[MyType] = {
      val convertedResults = dbo.asInstanceOf[BasicDBList].toList.map {
        case s:String => EventAction.values.find(_.toString == s)//  Some(EventAction.withName(s))
        case _ => None
      }
      if(convertedResults.contains(None))
        setBox(Failure("Error parsing database value into an Enumeration."))
      else
        setBox(Full(convertedResults.flatten)) // flatMap, collect ???
    }
  }


  // FIXME: move or copy eventType from EventLog ???
}

object Project extends Project with InjectableMetaRecord[Project] {
  override def collectionName = CollectionNames.PROJECT.toString
}

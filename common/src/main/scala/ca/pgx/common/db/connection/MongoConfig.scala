package ca.pgx.common.db.connection

import net.liftweb.common._
import net.liftweb.mongodb._
import net.liftweb.util.Props
import com.mongodb.ServerAddress
import com.mongodb.MongoException
import com.mongodb.WriteConcern
import net.liftweb.http.Factory
import com.mongodb.MongoClient

/**
 * Defines default MongoDB connection for Record based entity classes and Rogue DSL.
 * All DB classes that don't override 'mongoIdentifier' will use this connection.
 */
object MongoConfig extends Factory with Loggable {

  val defaultHostname = Props.get("mongo.host", "127.0.0.1")
  val defaultPort = Props.getInt("mongo.port", 27017)
  val defaultDbName = Props.get("mongo.dbname", "eventhub")

  /**
   * Defines default Mongo connection provider based on provided settings and falls back to Props defaults.
   * Call this method multiple times if default connection needs to be redefined.
   * This is a first method to call before ANY DB operations.
   */
  def init(hostname: String = defaultHostname, port: Int = defaultPort, dbName: String = defaultDbName) {
    val dbConnection = getConnection(hostname, port)
    MongoDB.defineDb(DefaultMongoIdentifier, dbConnection, dbName)
  }

  def getConnection(hostname: String = defaultHostname, port: Int = defaultPort): MongoClient = {
    val server = new ServerAddress(hostname, port)
    val dbConnection = new MongoClient(server)
    dbConnection setWriteConcern(WriteConcern.ACKNOWLEDGED)
    dbConnection
  }

  /**
   * Checks if there is connectivity to MongoDB server by
   * checking default DB connection and calling getLastError.
   */
  def isConnected = {
    val db = MongoDB.getDb(identifier.vend)
    if (db.isEmpty)
      false
    else
      try {
        db.get.getCollectionNames()
        true
      } catch {
        case t: MongoException => false
      }
  }

  //  def getDb = {
  //    MongoDB.getDb(DefaultMongoIdentifier)
  //  }

  val identifier = new FactoryMaker[MongoIdentifier](DefaultMongoIdentifier) {}

}

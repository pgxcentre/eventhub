package ca.pgx.rest.service

import ca.pgx.common.db.connection.MongoConfig
import ca.pgx.common.db.schema.mongeez.DbSchemaMigration
import ca.pgx.common.errorhandling.{CriticalExceptionHandler, CriticalExceptionHandlerImpl}
import net.liftweb.common.{Full, Logger}
import scala.sys.SystemProperties
import java.io.FileInputStream
import net.liftweb.util.{Props => LiftProps}

/**
 * Database initialization and migration tasks. They should run before application is ready to service requests.
 */
trait BootDbInit extends Logger with CriticalExceptionHandlerImpl {
  self: CriticalExceptionHandler =>

  /**
   * Runs all initialization tasks. Each task can be overriden if customization is required.
   */
  def initDb(dbName: String = MongoConfig.defaultDbName): Unit = {
    initMongo(dbName)
    migrateMongo(dbName)
  }

  def initMongo(thisDbName: String = MongoConfig.defaultDbName): Unit =
    doOrDie {
      MongoConfig.init(dbName = thisDbName)
      info(s"Connected to MongoDB database [$thisDbName].")
    }

  // FIXME: currently does not exit application as expected due to the bug:
  // https://github.com/secondmarket/mongeez/issues/17
  // if it does not get fixed very soon we can implement custom ChangeSetFileProvider and set it on Mongeez.
  def migrateMongo(thisDbName: String = MongoConfig.defaultDbName): Unit =
    doOrDie {
      DbSchemaMigration.run(dbName = thisDbName)
      info(s"Mongeez finished schema migration on database [$thisDbName].")
    }
}

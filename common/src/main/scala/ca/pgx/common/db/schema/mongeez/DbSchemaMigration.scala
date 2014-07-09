package ca.pgx.common.db.schema.mongeez

import ca.pgx.common.db.connection.MongoConfig
import net.liftweb.mongodb.MongoDB
import org.springframework.core.io.ClassPathResource
import org.mongeez.Mongeez

/**
 * Defines all MongoDB schema migrations using Mongeez.
 * Requires: default MongoDB connection to be initialized prior to use.
 */
object DbSchemaMigration {

  /**
   * Runs all migrations that were not run previously.
   */
  def run(dbName: String = MongoConfig.defaultDbName) {
    val db = MongoDB.getDb(MongoConfig.identifier.vend).get.getMongo
    val mongeez = new Mongeez
    val mainFile = new ClassPathResource("mongeez/mongeez.xml")
    mongeez.setFile(mainFile)
    mongeez.setMongo(db)
    mongeez.setDbName(dbName)
    mongeez.process()
  }
}

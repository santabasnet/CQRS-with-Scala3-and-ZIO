package com.iict.services

import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*
import com.iict.model.DoobieTransactor
import com.iict.utils.*
import com.iict.model.Db.PgConfig
import io.circe.{Json, Encoder, Decoder}
import io.circe.syntax.*
import io.circe.generic.semiauto.*
import io.circe.generic.auto.*

import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.circe.jsonb.implicits.*
import cats.implicits.*
import cats.syntax.all.*
import doobie.hikari.*
import doobie.util.log.LogEvent
import com.zaxxer.hikari.HikariConfig
import java.util.Properties
import javax.sql.DataSource
import com.iict.services.DoobieService.doobieTransactor
import com.iict.repository.CQRSModelDoobie
import java.time.OffsetDateTime
import java.util.UUID
import com.iict.model.*
import com.iict.utils.*
import com.iict.model.WriteModels.AttributeValues
import com.iict.model.ReadModels.*

class EntityService(doobieTransactor: DoobieTransactor) {

  /** Clear all the tables, It is needed for demo purpose, with manual data in
    * batch inserts and its subsequent execution.
    */
  def clearAll = (for {
    _ <- deleteFromEntityRecord
    _ <- deleteFromQueue
    _ <- deleteFromAttributeValues
    _ <- deleteFromAttributes
    _ <- deleteFromEntities
  } yield ()).transact(doobieTransactor.transactor)

  def insertAll = for {
    _ <- insertEntities
    _ <- insertAttributes
    _ <- insertAttributeValues(DemoData.shivAttributeValues)
    _ <- insertQueueItem(DemoData.shivAttributeValues)
    _ <- insertAttributeValues(DemoData.parbatiAttributeValues)
    _ <- insertQueueItem(DemoData.parbatiAttributeValues)
  } yield ()

  def insertEncounterRecord(
      record: EntityRecord,
      transactor: DoobieTransactor
  ) = CQRSModelDoobie
    .insertEntityItem(record)
    .transact(doobieTransactor.transactor)

  /** Insert Entities */
  private def insertEntities = CQRSModelDoobie
    .bulkInsertEntity(DemoData.entities)
    .transact(doobieTransactor.transactor)

  /** Insert Attributes * */
  private def insertAttributes = CQRSModelDoobie
    .bulkInsertAttribute(DemoData.attributes)
    .transact(doobieTransactor.transactor)

  /** Build an item to process further to put in the Read Model. */
  private def buildQueueItem(
      personItems: List[AttributeValues]
  ) = WriteModels
    .Queue(
      id = UUID.randomUUID,
      title = "Entity Job",
      attributes = personItems.asJson,
      status = QueueStatus.Todo,
      createdAt = OffsetDateTime.now,
      modifiedAt = OffsetDateTime.now
    )

  /** Insert Attribute Values */
  private def insertAttributeValues(items: List[AttributeValues]) = for {
    _ <- CQRSModelDoobie
      .bulkInsertAttributeValues(items)
      .transact(doobieTransactor.transactor)
  } yield ()

  private def insertQueueItem(items: List[AttributeValues]) = CQRSModelDoobie
    .insertQueueItem(buildQueueItem(items))
    .transact(doobieTransactor.transactor)

  /** Insert Attributes */

  private def deleteFromEntityRecord = for {
    _ <- CQRSModelDoobie.deleteEntityRecordQuery.update.run
    _ = println("--Deleted records from EntityRecords.--")
  } yield ()

  private def deleteFromQueue = for {
    _ <- CQRSModelDoobie.deleteQueueQuery.update.run
    _ = println("--Deleted records from Jobs in Queue.--")
  } yield ()

  private def deleteFromAttributeValues = for {
    _ <- CQRSModelDoobie.deleteAttributeValuesQuery.update.run
    _ = println("--Deleted records from Attribute Values.--")
  } yield ()

  private def deleteFromEntities = for {
    _ <- CQRSModelDoobie.deleteEntitiesQuery.update.run
    _ = println("--Deleted records from Jobs in Entities.--")
  } yield ()

  private def deleteFromAttributes = for {
    _ <- CQRSModelDoobie.deleteAttributesQuery.update.run
    _ = println("--Deleted records from Attributes.--")
  } yield ()
}

object EntityService:
  def service(transactor: DoobieTransactor) = ZIO.succeed(
    EntityService(transactor)
  )

object DoobieService:
  private def getTransactor(pgConfig: PgConfig) = {
    val PgConfig(host, port, name, username, password) = pgConfig
    val props = new Properties()
    props.setProperty(
      "dataSourceClassName",
      "org.postgresql.ds.PGSimpleDataSource"
    )
    props.setProperty("dataSource.user", username)
    props.setProperty("dataSource.password", password)
    props.setProperty("dataSource.databaseName", name)
    props.setProperty("dataSource.portNumber", port)
    props.setProperty("dataSource.serverName", host)

    val printSqlLogHandler: LogHandler[Task] = new LogHandler[Task] {
      def run(logEvent: LogEvent): Task[Unit] =
        Console.printLine(logEvent.sql)
    }

    val hikariConfig = new HikariConfig(props)
    HikariTransactor
      .fromHikariConfig[Task](
        hikariConfig,
        Some(printSqlLogHandler)
      )
      .toScopedZIO
  }

  private def defaultConfig = PgConfig(
    dbHost = "localhost",
    dbPort = "5432",
    dbName = "cqrs",
    dbUsername = "cqrs",
    dbPassword = "cqrs"
  )

  def doobieTransactor = for {
    pgConfig <- ZIO.succeed(defaultConfig)
    _transactor <- getTransactor(pgConfig)
    transactor = DoobieTransactor(_transactor)
  } yield transactor

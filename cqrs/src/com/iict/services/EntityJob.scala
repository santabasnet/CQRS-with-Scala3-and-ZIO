package com.iict.services

import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*
import cats.implicits.*
import cats.syntax.all.*
import io.circe.{Json, Encoder, Decoder}
import io.circe.syntax.*
import io.circe.generic.semiauto.*
import io.circe.generic.auto.*

import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.circe.jsonb.*
import doobie.postgres.circe.jsonb.implicits.*

import java.util.UUID
import com.iict.utils.JobTrigger.*
import com.iict.model.DoobieTransactor
import com.iict.repository.CQRSModelDoobie
import com.iict.model.WriteModels.{Queue, AttributeValues, Entities, Attributes}
import com.iict.model.ReadModels.*
import java.time.OffsetDateTime

case class EntityQueuedJob(transactor: DoobieTransactor) extends QueuedJob {

  /** Implementation of jobs running over the queued jobs in the background. */
  override def runActivities = for {
    statusRef <- Ref.make(false)
    entityJob = EntityJob(transactor).run(statusRef)
    _ <- entityJob.repeat(Schedule.fixed(TRIGGER_ENTITY_JOBS))
  } yield ()

}

object EntityQueuedJob {
  def service(
      transactor: DoobieTransactor
  ) = ZIO.succeed(EntityQueuedJob(transactor))
}

/** Entity Job Definition and Implementation. */
case class EntityJob(doobieTransactor: DoobieTransactor) {

  /** Execute the service job. */
  private def executeTask = ReadModelBuilder.build(doobieTransactor)

  private def whenBusy = ZIO.unit

  private def whenAvailable(jobStatus: Ref[Boolean]) = for {
    _ <- jobStatus.set(true)
    _ <- executeTask
    _ <- jobStatus.set(false)
  } yield ()

  /** Run the Job when it is available and gets a trigger. */
  private def runJobIfNotBusy(jobStatus: Ref[Boolean]) = for {
    status <- jobStatus.get
    _ <- status match
      case true  => whenBusy
      case false => whenAvailable(jobStatus)
  } yield ()

  def run(jobStatus: Ref[Boolean]) = runJobIfNotBusy(jobStatus)
}

object ReadModelBuilder {

  /** Load the data from write model and process it one by one to put in the
    * read data model.
    */
  def build(transactor: DoobieTransactor) = for {
    jobIds <- listQueuedJobIds(transactor)
    _ = println("\nProcessing Number of Jobs: " + jobIds.size)
    _ <- ZIO.foreach(jobIds)(id => { processJobId(id, transactor) })
  } yield ()

  /** List all the job ids, are put in the Job Queue. */
  private def listQueuedJobIds(transactor: DoobieTransactor) = for {
    jobIdsQueued <- CQRSModelDoobie.todoJobIdsQuery
      .query[UUID]
      .to[List]
      .transact(transactor.transactor)
  } yield jobIdsQueued

  /** Extract entity id from serialized json data. */
  private def extractEntityId(item: Queue): Option[UUID] =
    item.attributes.as[List[AttributeValues]] match
      case Right(values) => values.headOption.map(_.entityId)
      case Left(_)       => Option.empty[UUID]

  /** Needs to work with non-empty list. */
  private def extractAttributeIds(item: Queue): Option[List[UUID]] = {
    val ids = item.attributes.as[List[AttributeValues]] match
      case Right(values) => values.map(_.attributeId)
      case Left(_)       => List.empty[UUID]
    ids match
      case Nil           => Option.empty[List[UUID]]
      case x: List[UUID] => Some(ids)
  }

  private def loadQueuedItem(
      id: UUID,
      transactor: DoobieTransactor
  ) = for {
    jobs <- CQRSModelDoobie
      .todoQueueItem(id)
      .query[Queue]
      .to[List]
      .transact(transactor.transactor)
  } yield (jobs.headOption)

  private def loadEntity(
      id: UUID,
      transactor: DoobieTransactor
  ) = for {
    entities <- CQRSModelDoobie
      .loadEntities(id)
      .query[Entities]
      .to[List]
      .transact(transactor.transactor)
  } yield (entities.headOption)

  private def loadAttributes(
      ids: List[UUID],
      transactor: DoobieTransactor
  ) = for {
    attributes <- CQRSModelDoobie
      .loadAttributes(ids)
      .query[Attributes]
      .to[List]
      .transact(transactor.transactor)
  } yield (attributes)

  private def buildAttributeValues(
      attributesOpt: Option[List[Attributes]],
      queuedItemOpt: Option[Queue]
  ): Map[String, Json] = attributesOpt
    .zip(queuedItemOpt)
    .map((attributes, item) => {
      val attrValues = item.attributes.as[List[AttributeValues]] match
        case Right(values) => values
        case Left(_)       => List.empty[AttributeValues]
      attrValues
        .flatMap(aValue =>
          attributes
            .find(_.id == aValue.attributeId)
            .map(attr => attr.label -> aValue.value)
        )
        .toMap
    }) match
    case Some(x) => x
    case None    => Map.empty[String, Json]

  def processJobId(
      id: UUID,
      transactor: DoobieTransactor
  ) = {
    println("Processing With JobID: " + id)
    for {
      queueItemOpt <- loadQueuedItem(id, transactor)
      entityIdOpt = queueItemOpt.flatMap(extractEntityId)
      attributeIdsOpt = queueItemOpt.flatMap(extractAttributeIds)

      entityOpts <- entityIdOpt.traverse(id => loadEntity(id, transactor))
      entityOpt = entityOpts.flatten

      attributesOpt <- attributeIdsOpt.traverse(ids =>
        loadAttributes(ids, transactor)
      )

      recordOpt = entityOpt.map(e =>
        EntityRecord(
          id = UUID.randomUUID,
          label = e.label,
          attributes = buildAttributeValues(attributesOpt, queueItemOpt).asJson,
          createdAt = OffsetDateTime.now,
          modifiedAt = OffsetDateTime.now
        )
      )

      dataService <- EntityService.service(transactor)
      _ <- recordOpt.traverse(r =>
        dataService.insertEncounterRecord(r, transactor)
      )
      _ <- queueItemOpt.traverse(
        CQRSModelDoobie
          .updateQueueItem(_, QueueStatus.Completed)
          .transact(transactor.transactor)
      )
    } yield ()

  }
}

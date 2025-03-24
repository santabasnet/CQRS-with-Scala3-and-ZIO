package com.iict.repository

import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.circe.jsonb.implicits.*
import com.iict.model.ReadModels.QueueStatus
import com.iict.model.*
import com.iict.model.WriteModels.*
import com.iict.model.ReadModels.*
import java.util.UUID
import java.time.OffsetDateTime

object CQRSModelDoobie {

  /** Job Queue Table * */
  val jobQueueTable = "queue"

  /** Delete query of all items with given Table name. */
  private def deleteTable(tableName: String) = Fragment.const(
    s"DELETE FROM $tableName WHERE TRUE;"
  )

  /** Delete Query from EntityRecord. */
  def deleteEntityRecordQuery = deleteTable("entity_record")

  /** Delete Query from Queue. */
  def deleteQueueQuery = deleteTable("queue")

  /** Delete Query from Attribute Values. */
  def deleteAttributeValuesQuery = deleteTable("attribute_values")

  /** Delete Query from Attributes. */
  def deleteAttributesQuery = deleteTable("attributes")

  /** Delete Query from Entities. */
  def deleteEntitiesQuery = deleteTable("entities")

  /** Query to list all the Todo job Ids. Offset and Limit are to define the
    * Maximum batch size.
    */
  def todoJobIdsQuery: Fragment = todoJobIdsQuery(limit = 50, offset = 0)

  def addTableName(tableName: String)(fieldName: String) =
    s"$tableName.$fieldName"

  def columnsList = DoobieProduct
    .createFieldList[Queue]
    .map(addTableName("queue"))

  /** Can use macro expansion for columns name and attach to the select part. */
  def todoQueueItem(id: UUID): Fragment = {
    val columns = columnsList.mkString(", ")
    val selectPart = Fragment.const(s"SELECT $columns FROM queue")
    val wherePart = Fragment.const(s"WHERE queue.id = '$id'")
    selectPart ++ wherePart
  }

  def loadEntities(id: UUID): Fragment = {
    val selectPart = fr"SELECT * FROM entities"
    val wherePart = fr"WHERE id = ${id}"
    selectPart ++ wherePart
  }

  def loadAttributes(ids: List[UUID]) = {
    val idsText = ids.map(id => s"'$id'").mkString(",")
    val selectPart = fr"SELECT * FROM attributes"
    val wherePart = Fragment.const(s"WHERE id IN ($idsText)")
    selectPart ++ wherePart
  }

  /** Query to list all the Todo job Ids with given parameters. */
  private def todoJobIdsQuery(limit: Int, offset: Int): Fragment = {
    val selectPart = fr"SELECT id FROM queue"
    val wherePart = fr"WHERE queue.status = ${QueueStatus.Todo}"
    val limitPart = fr"LIMIT $limit OFFSET $offset"
    selectPart ++ wherePart ++ limitPart
  }

  /** Insert Entity */
  def bulkInsertEntity(entities: List[Entities]) =
    Update[Entities](DoobieInsert.insert[Entities]("entities"))
      .updateMany(entities)

  /** Insert Entity */
  def bulkInsertAttribute(attributes: List[Attributes]) =
    Update[Attributes](DoobieInsert.insert[Attributes]("attributes"))
      .updateMany(attributes)

  /** Insert Values for the associated entity and attribute. */
  def insertAttributeValue(value: AttributeValues) =
    Update[AttributeValues](
      DoobieInsert.insert[AttributeValues]("attribute_values")
    )
      .run(value)

  def insertEntityItem(record: EntityRecord) =
    Update[EntityRecord](DoobieInsert.insert[EntityRecord]("entity_record"))
      .run(record)

  /** Insert an Item to the Queue */
  def insertQueueItem(item: Queue) =
    Update[Queue](DoobieInsert.insert[Queue]("queue")).run(item)

  def bulkInsertAttributeValues(items: List[AttributeValues]) =
    Update[AttributeValues](
      DoobieInsert.insert[AttributeValues]("attribute_values")
    ).updateMany(items)

  def updateQueueItem(
      job: Queue,
      status: QueueStatus
  ) = {
    val updateStatusQuery = DoobieInsert.upsert[Queue]("queue")
    val updatedJob = job.copy(status = status, modifiedAt = OffsetDateTime.now)
    Update[Queue](updateStatusQuery).updateMany(List(updatedJob))
  }

}

package com.iict.model

import java.time.OffsetDateTime
import java.util.UUID

import io.circe.{Json, Encoder, Decoder}
import io.circe.generic.semiauto.*
import sttp.tapir.Schema
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto._
import com.iict.utils.*
import doobie.*
import com.iict.model.ReadModels.QueueStatus

/** This class is a part of the package com.iict.model and the package is a part
  * of the project wisemd-backend.
  *
  * Semantro/Integrated ICT Pvt. Ltd. Lalitpur, Nepal.
  * https://integratedict.com.np/ https://semantro.com/
  *
  * Created by santa on 2025-03-22. https://github.com/santabasnet
  */

object WriteModels {

  case class Entities(
      id: UUID,
      label: String,
      createdAt: OffsetDateTime,
      modifiedAt: OffsetDateTime
  )

  object Entities:
    given Schema[Entities] = Schema.derived
    given Encoder[Entities] = deriveEncoder[Entities]
    given Decoder[Entities] = deriveDecoder[Entities]

  case class Attributes(
      id: UUID,
      label: String,
      createdAt: OffsetDateTime,
      modifiedAt: OffsetDateTime
  )

  object Attributes:
    given Schema[Attributes] = Schema.derived
    given Encoder[Attributes] = deriveEncoder[Attributes]
    given Decoder[Attributes] = deriveDecoder[Attributes]

  case class AttributeValues(
      id: UUID,
      entityId: UUID,
      attributeId: UUID,
      value: Json,
      createdAt: OffsetDateTime,
      modifiedAt: OffsetDateTime
  )

  object AttributeValues:
    given Schema[AttributeValues] = Schema.derived
    given Encoder[AttributeValues] = deriveEncoder[AttributeValues]
    given Decoder[AttributeValues] = deriveDecoder[AttributeValues]

  case class Queue(
      id: UUID,
      title: String,
      attributes: Json,
      status: QueueStatus,
      createdAt: OffsetDateTime,
      modifiedAt: OffsetDateTime
  )

  object Queue:
    given Schema[Queue] = Schema.derived
    given Encoder[Queue] = deriveEncoder[Queue]
    given Decoder[Queue] = deriveDecoder[Queue]

}

object ReadModels {

  enum QueueStatus:
    case Todo, Started, Completed, Failed

  object QueueStatus:
    given Decoder[QueueStatus] = enumDecoder[QueueStatus]
    given Encoder[QueueStatus] = enumEncoder[QueueStatus]
    given Get[QueueStatus] = enumGet[QueueStatus]
    given Put[QueueStatus] = enumPut[QueueStatus]

  case class EntityRecord(
      id: UUID,
      label: String,
      attributes: Json,
      createdAt: OffsetDateTime,
      modifiedAt: OffsetDateTime
  )

  object EntityRecord:
    given Schema[EntityRecord] = Schema.derived
    given Encoder[EntityRecord] = deriveEncoder[EntityRecord]
    given Decoder[EntityRecord] = deriveDecoder[EntityRecord]

}

object Db:
  case class PgConfig(
      dbHost: String,
      dbPort: String,
      dbName: String,
      dbUsername: String,
      dbPassword: String
  )

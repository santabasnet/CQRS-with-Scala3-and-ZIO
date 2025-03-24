package com.iict.utils

import io.circe.{Json, Encoder, Decoder}
import io.circe.syntax.*
import io.circe.generic.semiauto.*
import io.circe.generic.auto.*

import com.iict.model.WriteModels.*
import java.time.OffsetTime
import java.time.OffsetDateTime
import java.util.UUID

object DemoData {

  /** Demo data for the entities. */
  val person = Entities(
    id = UUID.randomUUID,
    label = "Person",
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val entities: List[Entities] = List(person)

  /** Demo Data for the Attributes. */
  val attributeName = Attributes(
    id = UUID.randomUUID,
    label = "Name",
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val attributeGender = Attributes(
    id = UUID.randomUUID,
    label = "Gender",
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val attributeCity = Attributes(
    id = UUID.randomUUID,
    label = "City",
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val attributeContact = Attributes(
    id = UUID.randomUUID,
    label = "Contact Address",
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val attributeEmail = Attributes(
    id = UUID.randomUUID,
    label = "Email Address",
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val attributes = List(
    attributeName,
    attributeGender,
    attributeCity,
    attributeContact,
    attributeEmail
  )

  /** -- Person: values for Shiv. ---- */
  val personNameShiv = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeName.id,
    value = "Shiv Kumar Bantawa".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val personGenderShiv = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeGender.id,
    value = "Male".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val personCityShiv = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeCity.id,
    value = "Kathmandu".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val personContactShiv = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeContact.id,
    value = "Kalanki, Ward No. 39, Kathmandu, Nepal".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val personEmailShiv = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeEmail.id,
    value = "shiv_bantawa@gmail.com".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val shivAttributeValues = List(
    personNameShiv,
    personGenderShiv,
    personCityShiv,
    personContactShiv,
    personEmailShiv
  )

  /** ---------------------------------------------------- */

  /** -- Person: values for Shiv. ---- */
  val personNameParbati = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeName.id,
    value = "Parbati Kumar Shah".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val personGenderParbati = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeGender.id,
    value = "Female".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val personCityParbati = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeCity.id,
    value = "Kathmandu".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val personContactParbati = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeContact.id,
    value = "Balkhu, Ward No. 40, Kathmandu, Nepal".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val personEmailParbati = AttributeValues(
    id = UUID.randomUUID,
    entityId = person.id,
    attributeId = attributeEmail.id,
    value = "parbati_shah@gmail.com".asJson,
    createdAt = OffsetDateTime.now,
    modifiedAt = OffsetDateTime.now
  )

  val parbatiAttributeValues = List(
    personNameParbati,
    personGenderParbati,
    personCityParbati,
    personContactParbati,
    personEmailParbati
  )

  /** ---------------------------------------------------- */

}

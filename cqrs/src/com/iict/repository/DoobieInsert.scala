package com.iict.repository

import cats.syntax.all.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import com.iict.repository.DoobieProduct
import scala.deriving.Mirror

object DoobieInsert {
  def generateConflictUpdate(
      excludedFieldList: String,
      conflictFieldList: String
  ) = s"on conflict (${conflictFieldList}) do update set ${excludedFieldList}"

  def generateInsertQuery(
      tableName: String,
      fieldList: String,
      valuesList: String
  ) = List(
    "INSERT INTO",
    tableName,
    fieldList,
    "VALUES",
    valuesList
  ).mkString(" ")

  inline def conflict[T <: Product](using
      Mirror.ProductOf[T]
  ) = generateConflictUpdate(
    DoobieProduct.createExcludedFieldList[T],
    "id"
  )

  inline def insert[T <: Product](
      tableName: String
  )(using Mirror.ProductOf[T]) =
    val fieldList = DoobieProduct.createInsertFieldList[T]
    val valuesList = DoobieProduct.createValuesClause[T]
    generateInsertQuery(tableName, fieldList, valuesList)

  inline def upsert[T <: Product](tableName: String)(using
      Mirror.ProductOf[T]
  ) = insert[T](tableName) ++ " " ++ conflict[T]
}

package com.iict.repository

import doobie.*
import doobie.implicits.*
import cats.implicits.*
import cats.syntax.all.*
import scala.deriving.Mirror
import io.getquill.SnakeCase
import scala.compiletime.constValueTuple

object DoobieProduct {

  def generateExcluded(fieldList: List[String]) = fieldList
    .map(fieldName => s"$fieldName = excluded.$fieldName")
    .mkString(",")

  inline def getFieldNames[T <: Product](using
      m: Mirror.ProductOf[T]
  ): List[String] = constValueTuple[m.MirroredElemLabels].toList
    .asInstanceOf[List[String]]

  inline def createFieldList[T <: Product](using Mirror.ProductOf[T]) =
    getFieldNames[T].map(SnakeCase.default)

  inline def createInsertFieldList[T <: Product](using Mirror.ProductOf[T]) =
    s"(${createFieldList[T].mkString(",")})"

  inline def createValuesClause[T <: Product](using Mirror.ProductOf[T]) =
    s"(${getFieldNames[T].map(_ => "?").mkString(",")})"

  inline def createExcludedFieldList[T <: Product](using Mirror.ProductOf[T]) =
    val fieldList = createFieldList[T]
    generateExcluded(fieldList)

}

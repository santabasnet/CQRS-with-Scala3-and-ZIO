package com.iict.utils

import io.circe.*
import scala.deriving.Mirror
import doobie.{Get, Put}
import scala.deriving.Mirror
import scala.compiletime.summonAll
import scala.deriving.Mirror
import com.iict.utils.getOrThrow

inline def getSumInstances[T](using m: Mirror.SumOf[T]) =
  summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator
    .asInstanceOf[Iterator[ValueOf[T]]]
    .map(_.value)

inline def getSumLabels[T](using m: Mirror.SumOf[T]) =
  summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator
    .asInstanceOf[Iterator[ValueOf[String]]]
    .map(_.value)

inline def getEnumFromString[T](x: String)(using m: Mirror.SumOf[T]) =
  val labels = getSumLabels[T]
  val instances = getSumInstances[T]
  val instanceMap = labels.zip(instances).toMap
  instanceMap.get(x)

inline def enumGet[T](using
    m: Mirror.SumOf[T]
): Get[T] = Get[String].map(s =>
  getEnumFromString[T](s).getOrThrow(s"Invalid enum value: $s")
)

inline def enumPut[T](using
    m: Mirror.SumOf[T]
): Put[T] = Put[String].contramap(_.toString)

inline def enumDecoder[T](using m: Mirror.SumOf[T]): Decoder[T] =
  Decoder[String].emap { name =>
    getEnumFromString[T](name)
      .toRight(s"Invalid enumerator value: $name")
  }

inline def enumEncoder[T](using m: Mirror.SumOf[T]): Encoder[T] =
  val instances = getSumInstances[T]
  val labels = getSumLabels[T]
  val labelMap = instances.zip(labels).toMap
  Encoder[String].contramap[T](labelMap.apply)

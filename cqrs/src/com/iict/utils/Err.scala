package com.iict.utils

extension [T](o: Option[T]) {
  def getOrThrow(s: String) = o.getOrElse(throw new Error(s))
}

package com.iict.model

import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

import doobie.Transactor

class DoobieTransactor(
    _transactor: Transactor[Task]
):
  val transactor = _transactor

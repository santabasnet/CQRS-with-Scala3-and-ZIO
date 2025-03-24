package com.iict.app

import scalatags.Text.all._
import zio._
import com.iict.services._
import com.iict.model.DoobieTransactor

object Main extends ZIOAppDefault {

  def run = for {
    transactor <- DoobieService.doobieTransactor
    dataService <- EntityService.service(transactor)
    _ = println("\nClearing the data tables ...")
    _ <- dataService.clearAll

    _ = println("\nPerform Insert Operation on Demo Data ...")
    _ <- dataService.insertAll

    queuedJob <- EntityQueuedJob.service(transactor)
    _ <- queuedJob.runActivities

  } yield ()
}

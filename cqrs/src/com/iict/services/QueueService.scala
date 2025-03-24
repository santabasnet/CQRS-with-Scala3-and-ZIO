package com.iict.services

import zio.*
import com.iict.model.DoobieTransactor

/** A trait, that defines the behavior of queued jobs, and are implemented in
  * the backend module. These are the side jobs running in the different
  * background services.
  */
trait QueuedJob {
  def runActivities: ZIO[Any, Throwable, Unit]
}

object QueueService {
  val live = ZLayer {
    for {
      doobieTransactor <- ZIO.service[DoobieTransactor]
      queuedJob <- ZIO.service[QueuedJob]
      output <- queuedJob.runActivities
    } yield output
  }
}

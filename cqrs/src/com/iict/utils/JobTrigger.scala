package com.iict.utils

import zio.*

/** Literals for Job Trigger Service. */
object JobTrigger {

  /** Trigger Interval. */
  val TRIGGER_ENTITY_JOBS: Duration = 5.seconds

  /** Trigger Transplant Jobs. */
  val TRIGGER_TRANSPLANT_JOBS: Duration = 1.day

}

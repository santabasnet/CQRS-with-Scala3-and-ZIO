package com.iict.app

import utest._

object MainTests extends TestSuite {
  def tests = Tests {
    test("sample") {
      val expected = true
      val result = true
      assert(result == expected)
      result
    }
  }
}

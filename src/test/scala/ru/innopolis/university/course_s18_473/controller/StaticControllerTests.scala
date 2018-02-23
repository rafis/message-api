package ru.innopolis.university.course_s18_473.controller

import org.scalatra.test.scalatest._

class StaticControllerTests extends ScalatraFunSuite {

    addServlet(classOf[StaticController], "/*")

    test("GET / on StaticController should return status 200") {
        get("/") {
            status should equal (200)
            body should include ("Welcome to Scalatra")
        }
    }

}

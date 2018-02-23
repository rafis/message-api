package ru.innopolis.university.course_s18_473.controller

import org.json4s.{DefaultFormats, Formats, _}
import org.scalatra._
import org.scalatra.json._
import ru.innopolis.university.course_s18_473.data.Repository
import ru.innopolis.university.course_s18_473.auth.Auth

case class LoginRequest(val email: String, val password: String)

class SessionController(val repository: Repository) extends ScalatraServlet with JacksonJsonSupport {

    // Sets up automatic case class to JSON output serialization, required by
    // the JValueResult trait.
    protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

    // Before every action runs, set the content type to be in JSON format.
    before() {
        contentType = formats("json")
    }

    /**
      * Create a new JWT token.
      * POST /session
      */
    post("/") {
        val loginRequest: LoginRequest = parsedBody.extract[LoginRequest]

        val token = Auth.createSession(loginRequest, repository)

        ("token" -> token)
    }

}

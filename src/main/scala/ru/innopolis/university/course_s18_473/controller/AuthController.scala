package ru.innopolis.university.course_s18_473.controller

import org.json4s.{DefaultFormats, Formats, _}
import org.scalatra._
import org.scalatra.json._
import ru.innopolis.university.course_s18_473.data.Repository
import ru.innopolis.university.course_s18_473.auth.Auth

case class SignupRequest(val email: String, val password: String, val nickname: String)
case class LoginRequest(val email: String, val password: String)

class AuthController(val repository: Repository) extends ScalatraServlet with JacksonJsonSupport {

    // Sets up automatic case class to JSON output serialization, required by
    // the JValueResult trait.
    protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

    // Before every action runs, set the content type to be in JSON format.
    before() {
        contentType = formats("json")
    }

    /**
      * Sign up a new user (bot).
      * POST /user
      */
    post("/user") {
        val signupRequest: SignupRequest = parsedBody.extract[SignupRequest]

        val user = Auth.createUser(signupRequest, repository, true)

        val token = Auth.createSession(LoginRequest(user.email, signupRequest.password), repository)

        ("token" -> token)
    }

    /**
      * Create a new JWT token.
      * POST /session
      */
    post("/session") {
        val loginRequest: LoginRequest = parsedBody.extract[LoginRequest]

        val token = Auth.createSession(loginRequest, repository)

        ("token" -> token)
    }

}

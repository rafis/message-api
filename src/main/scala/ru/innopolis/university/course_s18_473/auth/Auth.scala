package ru.innopolis.university.course_s18_473.auth

import javax.servlet.http.HttpServletRequest

import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.{DefaultFormats, Formats, _}
import org.scalatra.{BadRequest, Control, NotFound, Unauthorized}
import pdi.jwt.{JwtAlgorithm, JwtJson4s}
import ru.innopolis.university.course_s18_473.controller.{LoginRequest, SignupRequest}
import ru.innopolis.university.course_s18_473.data.{Repository, User}

object Auth extends Control {

    // Sets up automatic case class to JSON output serialization, required by
    // the JValueResult trait.
    protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

    /**
      * Create a new user.
      *
      * @param signupRequest
      * @param repository
      * @param isBot
      * @return
      */
    def createUser(signupRequest: SignupRequest, repository: Repository, isBot: Boolean): User = {
        if ( ! repository.userStore.findByEmail(signupRequest.email).isEmpty) {
            halt(BadRequest("User with such email already exists"))
        }

        val user = repository.userStore.createUser(signupRequest.email, signupRequest.password, signupRequest.nickname)

        user
    }

    /**
      * Create a new JWT token.
      *
      * @param loginRequest
      * @param repository
      * @return
      */
    def createSession(loginRequest: LoginRequest, repository: Repository): String = {
        // Authenticate
        var user: User = repository.userStore.findByEmail(loginRequest.email).getOrElse(halt(NotFound(s"User with email=${loginRequest.email} not found")))
        if (UtilCrypto.generateHMAC("the_secret", loginRequest.password) != user.password) {
            halt(BadRequest("Authentication did not pass"))
        }

        // Generate token
        val secretKey = "some secret string used for crypto-things"
        val claim = JObject(("userId", user.id), ("exp", System.currentTimeMillis / 1000 + 24 * 60 * 60))
        val algo = JwtAlgorithm.HS256
        val token = JwtJson4s.encode(claim, secretKey, algo)

        token
    }

    /**
      * Validate that the incoming HTTP request has a valid JWT token and return the user.
      *
      * @param request
      * @param repository
      * @return
      */
    def validateAuth(request: HttpServletRequest, repository: Repository): Option[User] = {
        val secretKey = "some secret string used for crypto-things"
        val algo = JwtAlgorithm.HS256

        val authorizationHeader = Option(request.getHeader("Authorization"))
        val authorizationHeaderParsed = authorizationHeader.flatMap(_.split(" ") match {
            case Array(x, y, _*) => Some((x, y))
            case Array(x) => Some((x, ""))
            case _ => None
        })
        val isBearerAuth = (authorizationHeaderParsed.map{case (h, v) => h == "Bearer"}).getOrElse(false)
        val hasAuth = authorizationHeader.isDefined
        if ( ! hasAuth || ! isBearerAuth) {
            halt(Unauthorized("Authorization header is missing"))
        }
        val token = authorizationHeaderParsed.map{case (h, v) => v}.getOrElse(halt(Unauthorized("Authorization header is missing")))

        val claim: scala.util.Try[JObject] = JwtJson4s.decodeJson(token, secretKey, Seq(algo))
        if (claim.isFailure) {
            halt(Unauthorized("JWT token is invalid"))
        }
        val payload: JValue = claim match {
            case scala.util.Success(payload) => payload
            case _ => JObject()
        }
        val expires: Int = (payload \ "exp").extract[Int]
        if (expires < System.currentTimeMillis() / 1000) {
            halt(Unauthorized("JWT token has been expired, please reauthenticate."))
        }
        val userId: Int = (payload \ "userId").extract[Int]
        repository.userStore.findById(userId)
    }

}

package ru.innopolis.university.course_s18_473.controller

import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import ru.innopolis.university.course_s18_473.data.{Message, Repository, User}
import ru.innopolis.university.course_s18_473.auth.Auth

class FeedController(val repository: Repository) extends ScalatraServlet with JacksonJsonSupport {

    // Sets up automatic case class to JSON output serialization, required by
    // the JValueResult trait.
    protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

    // Before every action runs, set the content type to be in JSON format.
    before() {
        contentType = formats("json")
    }

    /**
      * Get my feed.
      * GET /feed
      */
    get("/") {
        val user: User = Auth.validateAuth(request, repository).getOrElse(halt(BadRequest("User not found")))
        val messages: Iterable[Message] = repository.messageStore.findAllByUsersIds(user.subscriptions.map(_.id) + user.id)
        messages
    }

    /**
      * Get public feed of any user.
      * GET /feed/:id
      */
    get("/:id") {
        val userId: Int = params.getAs[Int]("id").getOrElse(halt(BadRequest("Please provide an user ID")))
        var user: User = repository.userStore.findById(userId).getOrElse(halt(NotFound("User not found")))
        val messages: Iterable[Message] = repository.messageStore.findAllByUserId(userId)
        messages
    }

    /**
      * Subscribe me to specific user's feed.
      * POST /feed/:id/subscribe
      */
    post("/:id/subscribe") {
        val user: User = Auth.validateAuth(request, repository).getOrElse(halt(BadRequest("User not found")))
        val subscribeToUserId: Int = params.getAs[Int]("id").getOrElse(halt(BadRequest("Please provide an user ID")))
        val subscribeToUser: User = repository.userStore.findById(subscribeToUserId).getOrElse(halt(NotFound("User not found")))
        if (user == subscribeToUser) {
            halt(BadRequest("You can not subscribe to yourself"))
        }
        repository.userStore.subscribe(user, subscribeToUser)
        NoContent()
    }

    /**
      * Unsubscribe me to specific user's feed.
      * DELETE /feed/:id/subscribe
      */
    delete("/:id/subscribe") {
        val user: User = Auth.validateAuth(request, repository).getOrElse(halt(BadRequest("User not found")))
        val unsubscribeToUserId: Int = params.getAs[Int]("id").getOrElse(halt(BadRequest("Please provide an user ID")))
        val unsubscribeToUser: User = repository.userStore.findById(unsubscribeToUserId).getOrElse(halt(NotFound("User not found")))
        repository.userStore.unsubscribe(user, unsubscribeToUser)
        NoContent()
    }

}

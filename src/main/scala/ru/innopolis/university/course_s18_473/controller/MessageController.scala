package ru.innopolis.university.course_s18_473.controller

import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import ru.innopolis.university.course_s18_473._
import ru.innopolis.university.course_s18_473.data._
import ru.innopolis.university.course_s18_473.auth.Auth

class MessageController(val repository: Repository) extends ScalatraServlet with JacksonJsonSupport {

    // Sets up automatic case class to JSON output serialization, required by
    // the JValueResult trait.
    protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

    // Before every action runs, set the content type to be in JSON format.
    before() {
        contentType = formats("json")
    }

    /**
     * Get all messages.
     * GET /message
     */
    get("/") {
        repository.messageStore.list
    }

    /**
     * Get the message by ID.
     * GET /message/:id
     */
    get("/:id") {
        val messageId: Int = params.getAs[Int]("id").getOrElse(halt(BadRequest("Please provide an message ID")))
        val message: Message = repository.messageStore.findById(messageId).getOrElse(halt(NotFound(s"Message with id=${messageId} does not exists")))
        message
    }

    /**
     * Create a new message.
     * POST /message
     */
    post("/") {
        val user: User = Auth.validateAuth(request, repository).getOrElse(halt(BadRequest("User not found")))
        val messageCreate: MessageCreate = parsedBody.extract[MessageCreate]
        val message = repository.messageStore.createMessage(user.id, messageCreate)
        message
    }

    /**
     * Update the message specified by ID.
     * PUT /message/:id
     */
    put("/:id") {
        val user: User = Auth.validateAuth(request, repository).getOrElse(halt(BadRequest("User not found")))
        val messageId: Int = params.getAs[Int]("id").getOrElse(halt(BadRequest("Please provide an message ID")))
        val message = repository.messageStore.findById(messageId).getOrElse(halt(NotFound(s"Message with id=${messageId} does not exists")))
        if (message.userId != user.id) {
            halt(Unauthorized("Modification of other users messages is disallowed"))
        }
        val updatedMessage = message.copy(text = parsedBody.extract[MessageUpdate].text)
        repository.messageStore += (messageId -> updatedMessage)
        updatedMessage
    }

    /**
     * Delete the message specified by ID.
     * DELETE /message/:id
     */
    delete("/:id") {
        val user: User = Auth.validateAuth(request, repository).getOrElse(halt(BadRequest("User not found")))
        val messageId: Int = params.getAs[Int]("id").getOrElse(halt(BadRequest("Please provide an message ID")))
        val message = repository.messageStore.findById(messageId).getOrElse(halt(NotFound(s"Message with id=${messageId} does not exists")))
        if (message.userId != user.id) {
            halt(Unauthorized("Modification of other users messages is disallowed"))
        }
        repository.messageStore.deleteMessage(messageId)
        NoContent()
    }

    /**
      * Retweet the message.
      * POST /message/:id/retweet
      */
    post("/:id/retweet") {
        val user: User = Auth.validateAuth(request, repository).getOrElse(halt(BadRequest("User not found")))
        val messageId: Int = params.getAs[Int]("id").getOrElse(halt(BadRequest("Please provide an message ID")))
        val messageToRetweet = repository.messageStore.findById(messageId).getOrElse(halt(NotFound(s"Message with id=${messageId} does not exists")))
        if (messageToRetweet.userId == user.id) {
            halt(BadRequest("You can not retweet own messages"))
        }
        val message = repository.messageStore.retweetMessage(usesr.id, messageToRetweet)
        message
    }

}

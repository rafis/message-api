package ru.innopolis.university.course_s18_473.data

import scala.collection.mutable.HashMap

case class MessageCreate(val text: String)
case class MessageUpdate(val text: String)
case class Message(val id: Int, val userId: Int, val text: String, val timestamp: Long, val retweetedFrom: Option[Message])

/**
 * In-memory storage for messages.
 */
case class MessageStore() {

    protected val messages: HashMap[Int, Message] = HashMap.empty[Int, Message]
    protected var latestMessageId = 0

    def list: Iterable[Message] = MessageStore.sortedByTimestamp(messages)

    def isDefinedAt(id: Int): Boolean = messages.isDefinedAt(id)

    def findById(id: Int): Option[Message] = messages.get(id)

    def findAllByUserId(userId: Int): Iterable[Message] = {
        MessageStore.sortedByTimestamp(messages.filter(_._2.userId == userId))
    }

    def findAllByUsersIds(usersIds: Set[Int]): Iterable[Message] = {
        MessageStore.sortedByTimestamp(messages.filter(usersIds contains _._2.userId))
    }

    def createMessage(userId: Int, messageCreate: MessageCreate) = {
        if (latestMessageId >= Int.MaxValue) {
            throw new StackOverflowError("Database is full")
        }

        val message = Message(latestMessageId + 1, userId, messageCreate.text, System.currentTimeMillis / 1000, None)
        this += message
        latestMessageId += 1
        message
    }

    def retweetMessage(userId: Int, messageToRetweet: Message): Message = {
        if (latestMessageId >= Int.MaxValue) {
            throw new StackOverflowError("Database is full")
        }

        val message = Message(latestMessageId + 1, userId, messageToRetweet.text, System.currentTimeMillis / 1000, Some(messageToRetweet))
        this += message
        latestMessageId += 1
        message
    }

    def deleteMessage(messageId: Int) = {
        // First delete all retweets of the message
        for {
            (retweetMessageId, message) <- messages
            originalMessage <- message.retweetedFrom
            if originalMessage.id == messageId
        } {
            messages -= retweetMessageId
        }

        // Now delete the message itself
        this -= messageId
    }

    def +=(message: Message) = messages += (message.id -> message)

    def +=(updatedMessage: (Int, Message)) = messages += (updatedMessage._1 -> updatedMessage._2)

    def -=(messageId: Int) = messages -= messageId

}

/**
  * Some utility functions
  */
object MessageStore {

    def sortedByTimestamp(messages: Iterable[(Int, Message)]): Iterable[Message] = {
        messages.toSeq.sortBy(_._2.timestamp).map(_._2)
    }

}

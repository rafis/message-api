package ru.innopolis.university.course_s18_473.data

import ru.innopolis.university.course_s18_473.auth.{Auth, UtilCrypto}

import scala.collection.mutable.HashMap

case class User(val id: Int, val email: String, val password: String, val nickname: String, val isBot: Boolean = false, var subscriptions: Set[User] = Set.empty[User])

/**
 * In-memory storage for users.
 */
case class UserStore() {

    protected val users: HashMap[Int, User] = HashMap.empty[Int, User]
    protected var latestUserId = 0

    def list: Iterable[User] = users.values

    def isDefinedAt(id: Int): Boolean = users.isDefinedAt(id)

    def findById(id: Int): Option[User] = users.get(id)

    def findByEmail(email: String): Option[User] = users.find(_._2.email == email).map(_._2)

    def subscribe(user: User, subscribeToUser: User) = {
        user.subscriptions += subscribeToUser
    }

    def unsubscribe(user: User, unsubscribeFromUser: User) = {
        user.subscriptions -= unsubscribeFromUser
    }

    def createUser(email: String, password: String, nickname: String, isBot: Boolean) = {
        if (latestUserId >= Int.MaxValue) {
            throw new StackOverflowError("Database is full")
        }

        val user = User(latestUserId + 1, email, UtilCrypto.generateHMAC("the_secret", password), nickname, isBot)
        this += user
        latestUserId += 1
        user
    }

    def +=(addUser: User) = users += (addUser.id -> addUser)

    def -=(deleteUser: Int) = users -= deleteUser

}

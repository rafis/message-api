package ru.innopolis.university.course_s18_473.data

/**
 * In-memory database
 */
case class Repository(
    val messageStore: MessageStore,
    val userStore: UserStore
) { }

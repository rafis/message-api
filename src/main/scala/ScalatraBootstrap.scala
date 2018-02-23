import ru.innopolis.university.course_s18_473._
import org.scalatra._
import javax.servlet.ServletContext

import ru.innopolis.university.course_s18_473.controller.{FeedController, MessageController, AuthController, StaticController}
import ru.innopolis.university.course_s18_473.data.{MessageStore, Repository, User, UserStore}

class ScalatraBootstrap extends LifeCycle {
    override def init(context: ServletContext) {
        val messageStore = MessageStore()
        val userStore = UserStore()
        userStore.createUser("r.ganeev@innopolis.ru", "password", "rganeev", false)
        userStore.createUser("j.dealbuquerque@innopolis.ru", "password", "jdealbuquerque", false)
        //userStore.createUser("deepdrumpf@gmail.com", "password", "DeepDrumpf", true)
        val repository = Repository(messageStore, userStore)

        context.mount(new MessageController(repository), "/message/*")
        context.mount(new FeedController(repository), "/feed/*")
        context.mount(new AuthController(repository), "/auth/*")
        context.mount(new StaticController(), "/*")
    }
}

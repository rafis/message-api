package ru.innopolis.university.course_s18_473.controller

import org.scalatra._

class StaticController extends ScalatraServlet {

    get("/") {
        views.html.hello()
    }

}

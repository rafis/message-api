package ru.innopolis.university.course_s18_473.controller

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest._
import ru.innopolis.university.course_s18_473.data.{MessageStore, Repository, User, UserStore}

class MessageControllerTests extends ScalatraFunSuite {

    val messageStore = MessageStore()
    val userStore = UserStore()
    userStore += (1 -> User(1, "rafis", "1a8c342b4753fe45d2d03c8f24477d722a77c3f1", "Rafis Ganeev", false))
    userStore += (2 -> User(2, "julio", "1a8c342b4753fe45d2d03c8f24477d722a77c3f1", "Julio Reis", false))
    userStore += (3 -> User(3, "deepdrumpf", "1a8c342b4753fe45d2d03c8f24477d722a77c3f1", "DeepDrumpf", true))
    val repository = Repository(messageStore, userStore)
    addServlet(new MessageController(repository), "/messages/*")

    def postJson[A](uri: String, body: JValue, headers: Map[String, String] = Map.empty)(f: => A): A =
        post(uri, compact(render(body)).getBytes("utf-8"), Map("Content-Type" -> "application/json; charset=UTF-8") ++ headers)(f)

    def putJson[A](uri: String, body: JValue, headers: Map[String, String] = Map.empty)(f: => A): A =
        put(uri, compact(render(body)).getBytes("utf-8"), Map("Content-Type" -> "application/json; charset=UTF-8") ++ headers)(f)

    test("POST /messages/ on MessageController should create a new message") {
        postJson("/messages/", JObject(List("id" -> JInt(1), "text" -> JString("Test text")))) {
            status should equal (200)
        }
    }

    test("POST /messages/ on MessageController second time should generate a validation error") {
        postJson("/messages/", JObject(List("id" -> JInt(1), "text" -> JString("Test text")))) {
            status should be >= 400
            status should be < 500
        }
    }

    test("GET /message/ on MessageController should return list of messages including recently created message") {
        get("/messages/") {
            status should equal (200)
            header("Content-Type") should startWith ("application/json;")
            val json = parse(body)
            json(0) \ "id" should equal(JInt(1))
            json(0) \ "text" should equal(JString("Test text"))
        }
    }

    test("GET /messages/1 on MessageController should return the message with id=1") {
        get("/messages/1") {
            status should equal (200)
            header("Content-Type") should startWith ("application/json;")
            val json = parse(body)
            json \ "id" should equal(JInt(1))
            json \ "text" should equal(JString("Test text"))
        }
    }

    test("GET /messages/999 on MessageController should generate a Not Found error") {
        get("/messages/999") {
            status should equal (404)
        }
    }

    test("PUT /messages/1 on MessageController should update the text of the message") {
        putJson("/messages/1", JObject(List("text" -> JString("Some new text")))) {
            status should equal (200)
        }
    }

    test("PUT /messages/999 on MessageController should generate a Not Found error") {
        putJson("/messages/999", JObject(List("text" -> JString("Some new text")))) {
            status should equal (404)
        }
    }

    test("DELETE /messages/1 on MessageController should delete the message") {
        delete("/messages/1") {
            status should equal (204)
        }
    }

}

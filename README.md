# RESTful Message API #

A prototype of RESTful Message Web-service with the following functionality:

* Registration, Sign in using JWT Tokens
* Send messages to current user's feed, view feed
* Update, delete own messages
* View & subscribe to other users feeds

## Build & Run ##

```sh
$ cd message-api
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

## Test ##

To run tests (temporary broken):

```sh
$ sbt test
```

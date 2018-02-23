package ru.innopolis.university.course_s18_473.auth

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object UtilCrypto {

    def generateHMAC(secretKey: String, payload: String): String = {
        val secret = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(secret)
        val hashString: Array[Byte] = mac.doFinal(payload.getBytes())
        hashString.map("%02x" format _).mkString
    }

}

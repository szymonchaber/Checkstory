package dev.szymonchaber.checkstory.data.database

import java.nio.ByteBuffer
import java.util.*

object UUIDUtil {

    fun convertBytesToUUID(bytes: ByteArray): UUID {
        val buffer = ByteBuffer.wrap(bytes)
        val firstLong = buffer.long
        val secondLong = buffer.long
        return UUID(firstLong, secondLong)
    }

    fun convertUUIDToBytes(uuid: UUID): ByteArray {
        val bytes = ByteArray(16)
        val buffer = ByteBuffer.wrap(bytes)
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        return buffer.array()
    }
}

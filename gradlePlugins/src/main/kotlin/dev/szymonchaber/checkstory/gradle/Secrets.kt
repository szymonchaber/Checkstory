package dev.szymonchaber.checkstory.gradle

import groovy.lang.MissingPropertyException
import java.io.File
import java.io.FileNotFoundException
import java.util.*

object Secrets {

    private const val SECRETS_FILE_NAME = "secret.properties"

    fun getSecret(key: String): String = "\"${getSecretFromFile(key)}\""

    private fun getSecretFromFile(key: String): String {
        val secretsFile = File(SECRETS_FILE_NAME).takeIf { it.exists() }
            ?: throw FileNotFoundException("File $SECRETS_FILE_NAME not found!")
        val properties = secretsFile.reader().use {
            Properties().apply {
                load(it)
            }
        }
        val secret = properties.getProperty(key)
        if (secret.isNullOrEmpty()) {
            throw MissingPropertyException(
                "Missing property: $key. Verify configuration inside $SECRETS_FILE_NAME file."
            )
        }
        return secret
    }
}

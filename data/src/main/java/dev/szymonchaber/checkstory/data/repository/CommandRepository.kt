package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.CommandDao
import dev.szymonchaber.checkstory.domain.model.Command
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CommandRepository @Inject constructor(
    private val dao: CommandDao,
    private val commandMapper: CommandMapper
) {

    suspend fun getUnsynchronizedCommands(): List<Command> {
        return withContext(Dispatchers.Default) {
            dao.getAll().map(commandMapper::toDomainCommand)
        }
    }

    suspend fun storeCommands(commands: List<Command>) {
        withContext(Dispatchers.Default) {
            dao.insertAll(commands.map(commandMapper::toCommandEntity))
        }
    }

    suspend fun commandCount(): Int {
        return dao.getAll().size
    }

    suspend fun deleteCommands(ids: List<UUID>) {
        withContext(Dispatchers.IO) {
            ids.map {
                async {
                    dao.deleteById(it)
                }
            }.awaitAll()
        }
    }

    suspend fun deleteAllCommands() {
        dao.deleteAll()
    }
}

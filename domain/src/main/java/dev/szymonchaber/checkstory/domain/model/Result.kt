package dev.szymonchaber.checkstory.domain.model

sealed class Result<Error, Data> {

    data class Success<Error, Data>(val data: Data) : Result<Error, Data>()
    data class Error<Error, Data>(val error: Error) : Result<Error, Data>()

    companion object {

        fun <Error, Data> success(data: Data): Result<Error, Data> {
            return Success(data)
        }

        fun <Error, Data> error(error: Error): Result<Error, Data> {
            return Error(error)
        }
    }
}

suspend fun <Error, Data> Result<Error, Data>.tapSuccess(onSuccess: suspend (Data) -> Unit): Result<Error, Data> {
    if (this is Result.Success) {
        onSuccess(this.data)
    }
    return this
}

fun <T, Error, Data> Result<Error, Data>.mapSuccess(function: (Data) -> T): Result<Error, T> {
    return when (this) {
        is Result.Success -> {
            Result.success(function(this.data))
        }

        is Result.Error -> {
            Result.error(this.error)
        }
    }
}

fun <T, Error, Data> Result<Error, Data>.fold(
    mapError: (Error) -> T,
    mapSuccess: (Data) -> T,
): T {
    return when (this) {
        is Result.Error -> {
            mapError(this.error)
        }

        is Result.Success -> {
            mapSuccess(this.data)
        }
    }
}

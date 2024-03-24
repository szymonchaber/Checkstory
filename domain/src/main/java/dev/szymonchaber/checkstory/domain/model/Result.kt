package dev.szymonchaber.checkstory.domain.model

sealed class Result<Error, Data> {

    data class Success<Error, Data>(val data: Data) : Result<Error, Data>()
    data class Error<Error, Data>(val error: Error) : Result<Error, Data>()

    inline fun <T> mapSuccess(function: (Data) -> T): Result<Error, T> {
        return when (this) {
            is Success -> {
                success(function(this.data))
            }

            is Result.Error -> {
                error(this.error)
            }
        }
    }

    inline fun <T> flatMapSuccess(function: (Data) -> Result<Error, T>): Result<Error, T> {
        return when (this) {
            is Success -> {
                function(this.data)
            }

            is Result.Error -> {
                error(this.error)
            }
        }
    }

    fun <T> mapError(function: (Error) -> T): Result<T, Data> {
        return when (this) {
            is Success -> {
                success(data)
            }

            is Result.Error -> {
                error(function(this.error))
            }
        }
    }

    fun <T> handleError(function: (Error) -> Result<T, Data>): Result<T, Data> {
        return when (this) {
            is Success -> {
                success(data)
            }

            is Result.Error -> {
                function(this.error)
            }
        }
    }

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

inline fun <T, Error, Data> Result<Error, Data>.fold(
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

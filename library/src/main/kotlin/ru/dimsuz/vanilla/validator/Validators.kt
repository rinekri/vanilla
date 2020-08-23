package ru.dimsuz.vanilla.validator

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.map

// TODO document that for writing validators only, because inference problems, use compose in rules
fun <I, O1, O2, E> Validator<I, O1, E>.andThen(other: Validator<O1, O2, E>): Validator<I, O2, E> {
  return Validator { input ->
    when (val result = this.validate(input)) {
      is Result.Ok -> other.validate(result.value)
      is Result.Error -> result
    }
  }
}

fun <I, O1, O2, E> Validator<I, O1, E>.map(mapper: (O1) -> O2): Validator<I, O2, E> {
  return Validator { input -> this.validate(input).map(mapper) }
}

fun <I, E> ok(): Validator<I, I, E> {
  return Validator { Result.Ok(it) }
}

fun <T : Any, E> isNotNull(error: E): Validator<T?, T, E> {
  return Validator { input -> if (input != null) Result.Ok(input) else Result.Error(error) }
}

fun <E> isNotEmpty(error: E): Validator<String, String, E> {
  return Validator { input ->
    if (input.isNotEmpty()) Result.Ok(input) else Result.Error(error)
  }
}

fun <E> isNotBlank(errorProvider: (failedInput: String) -> E): Validator<String, String, E> {
  return Validator { input ->
    if (input.isNotBlank()) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <E> hasLengthGreaterThanOrEqualTo(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return Validator { input ->
    if (input.length >= length) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <E> hasLengthLessThanOrEqualTo(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return Validator { input ->
    if (input.length <= length) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <E> hasLengthGreaterThan(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return hasLengthGreaterThanOrEqualTo(length + 1, errorProvider)
}

fun <E> hasLengthLessThan(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return hasLengthLessThanOrEqualTo(length - 1, errorProvider)
}

fun <E> hasLengthInRange(
  min: Int,
  max: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  require(min <= max) { "invalid range ($min..$max), expected min <= max" }
  return Validator { input ->
    if (input.length in (min..max)) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <T : Comparable<T>, E> isLessThan(
  value: T,
  errorProvider: (failedInput: T) -> E
): Validator<T, T, E> {
  return Validator { input ->
    if (input < value) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <T : Comparable<T>, E> isLessThanOrEqual(
  value: T,
  errorProvider: (failedInput: T) -> E
): Validator<T, T, E> {
  return Validator { input ->
    if (input <= value) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <T : Comparable<T>, E> isGreaterThan(
  value: T,
  errorProvider: (failedInput: T) -> E
): Validator<T, T, E> {
  return Validator { input ->
    if (input > value) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <T : Comparable<T>, E> isGreaterThanOrEqual(
  value: T,
  errorProvider: (failedInput: T) -> E
): Validator<T, T, E> {
  return Validator { input ->
    if (input >= value) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

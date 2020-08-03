package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import ru.dimsuz.vanilla.annotation.ValidatedAs
import ru.dimsuz.vanilla.annotation.ValidatedName
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.either.Left
import ru.dimsuz.vanilla.processor.either.Right
import ru.dimsuz.vanilla.processor.either.join
import ru.dimsuz.vanilla.processor.either.lift3
import ru.dimsuz.vanilla.processor.either.toRightOr
import ru.dimsuz.vanilla.processor.extension.enclosingPackageName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

fun findValidationModelPairs(roundEnv: RoundEnvironment): Either<Error, List<ModelPair>> {
  return roundEnv.getElementsAnnotatedWith(ValidatedAs::class.java).map { element ->
    val sourceKmClass = (element as? TypeElement)?.toImmutableKmClass()
      .toRightOr("internal error: failed to read source model information")
    val targetKmClass = element.extractTargetModelClass()?.toImmutableKmClass()
      .toRightOr("internal error: failed to read target model information")
    val sourcePackage = Right(element.enclosingPackageName)
    lift3(sourceKmClass, targetKmClass, sourcePackage, ::ModelPair)
  }.join()
}

fun findMatchingProperties(models: ModelPair): Either<Error, PropertyMapping> {
  val sourceProps = models.source.properties.toSet()
  val targetProps = models.target.properties.toSet()
  val commonSourceProps = sourceProps.toMutableSet()
  commonSourceProps.retainAll { sProp -> targetProps.any { it.name == sProp.name } }
  return if (commonSourceProps.isEmpty()) {
    Left(
      "failed to find matching properties. Consider adding @${ValidatedName::class.java.simpleName} " +
        "annotation to properties of \"${models.source.name}\" class"
    )
  } else {
    Right(
      PropertyMapping(
        models = models,
        mapping = commonSourceProps.associateWith { sProp -> targetProps.first { it.name == sProp.name } }
      )
    )
  }
}

private fun Element.extractTargetModelClass(): TypeElement? {
  val baseClassType = try {
    this.getAnnotation(ValidatedAs::class.java).verifiedModel
    error("expected ${MirroredTypeException::class.java.simpleName} to be thrown")
  } catch (e: MirroredTypeException) {
    e.typeMirror
  }
  return ((baseClassType as? DeclaredType)?.asElement() as? TypeElement)
}

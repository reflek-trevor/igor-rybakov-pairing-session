package io.reflek.archunit

import com.tngtech.archunit.core.domain.{JavaClass, JavaClasses, JavaModifier}
import com.tngtech.archunit.core.importer.{ClassFileImporter, Location}

import java.lang.annotation.Annotation
import java.util.regex.Pattern
import scala.reflect.ClassTag

trait ArchUnitSpec {
  implicit class JavaClassOps(self: JavaClass) {
    def isConcrete: Boolean = {
      !(self.isInterface || self.getModifiers.contains(JavaModifier.ABSTRACT))
    }

    def hasAnnotation[T <: Annotation](implicit classTag: ClassTag[T]): Boolean = {
      self.isAnnotatedWith(classTag.runtimeClass.asInstanceOf[Class[? <: Annotation]])
    }
  }
}

object ArchUnitSpec {
  // Let's import classes just once in the object lifetime (and not once per test suite instance) to speed things up.
  private val classFileImporter         = new ClassFileImporter()
  lazy val importedClasses: JavaClasses = classFileImporter.importPackages("io.reflek")
  lazy val importedProductionClasses: JavaClasses = classFileImporter
    .withImportOption(
      (location: Location) => {
        !location.matches(Pattern.compile(".*/target/scala-.*/test-classes/.*"))
      }
    )
    .importPackages("io.reflek")
}

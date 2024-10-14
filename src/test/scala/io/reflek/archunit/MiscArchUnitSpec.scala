package io.reflek.archunit

import com.tngtech.archunit.core.domain.*
import com.tngtech.archunit.core.domain.JavaCall.Predicates.target
import com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo
import com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameMatching
import com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner
import com.tngtech.archunit.lang.{ArchCondition, ConditionEvents, SimpleConditionEvent}
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.{classes, noClasses}
import io.reflek.archunit.ArchUnitSpec.{importedClasses, importedProductionClasses}
import org.scalatest.wordspec.AnyWordSpecLike

class MiscArchUnitSpec extends AnyWordSpecLike with ArchUnitSpec {

  "Classes" should {

    "never call println(...)" in {
      noClasses.should
        .callMethodWhere {
          target {
            owner {
              equivalentTo(scala.Predef.getClass)
            }
          } `and` target {
            nameMatching("println")
          }
        }
        .check(importedClasses)
    }

    "return ReplyEffect from methods rather than Effect" in {
      classes
        .should(new ArchCondition[JavaClass]("return ReplyEffect from methods rather than Effect") {
          override def check(clazz: JavaClass, events: ConditionEvents): Unit = {
            clazz.getMethods.forEach {
              method =>
                if (method.getRawReturnType.isEquivalentTo(classOf[akka.persistence.typed.scaladsl.Effect[?, ?]])) {
                  val message = s"Method ${method.getFullName} returns an Effect; " +
                    "use ReplyEffect instead to make sure that all commands are replied to"
                  events.add(SimpleConditionEvent.violated(clazz, message))
                }
            }
          }
        })
        .check(importedProductionClasses)
    }
  }
}

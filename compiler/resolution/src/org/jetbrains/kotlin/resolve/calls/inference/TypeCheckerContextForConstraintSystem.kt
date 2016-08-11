/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.calls.inference

import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.*
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.utils.addToStdlib.check

abstract class TypeCheckerContextForConstraintSystem : TypeCheckerContext(errorTypeEqualsToAnything = true) {

    abstract fun isMyTypeVariable(type: SimpleType): Boolean

    abstract fun addUpperConstraint(typeVariable: SimpleType, superType: UnwrappedType)
    abstract fun addLowerConstraint(typeVariable: SimpleType, subType: UnwrappedType)

    override final fun addSubtypeConstraint(subType: UnwrappedType, superType: UnwrappedType): Boolean? {
        assertInputTypes(subType, superType)

        var answer: Boolean? = null

        if (superType.anyBound(this::isMyTypeVariable)) {
            answer = simplifyLowerConstraint(subType, superType)
        }

        if (subType.anyBound(this::isMyTypeVariable)) {
            return simplifyUpperConstraint(subType, superType) && (answer ?: true)
        }
        else {
            return simplifyConstraintForPossibleIntersectionSubType(subType, superType) ?: answer
        }
    }

    /**
     * Foo <: T! <=> Foo <: T? <=> Foo & Any <: T
     * Foo <: T? <=> Foo & Any <: T
     * Foo <: T -- leave as is
     */
    fun simplifyLowerConstraint(typeVariable: UnwrappedType, subType: UnwrappedType): Boolean {
        @Suppress("NAME_SHADOWING")
        val typeVariable = typeVariable.upperIfFlexible()

        if (typeVariable.isMarkedNullable) {
            addLowerConstraint(typeVariable, intersectTypes(listOf(subType, subType.builtIns.anyType)))
        }
        else {
            addLowerConstraint(typeVariable, subType)
        }

        return true
    }

    /**
     * T! <: Foo <=> T <: Foo
     * T? <: Foo <=> T <: Foo && Nothing? <: Foo
     * T  <: Foo -- leave as is
     */
    fun simplifyUpperConstraint(typeVariable: UnwrappedType, superType: UnwrappedType): Boolean {
        @Suppress("NAME_SHADOWING")
        val typeVariable = typeVariable.lowerIfFlexible()

        addUpperConstraint(typeVariable, superType)

        if (typeVariable.isMarkedNullable) {
            // here is important that superType is singleClassifierType
            return if (superType.anyBound(this::isMyTypeVariable)) {
                simplifyLowerConstraint(superType, typeVariable)
            }
            else {
                isSubtypeOfByTypeChecker(typeVariable.builtIns.nullableNothingType, superType)
            }
        }

        return true
    }

    fun simplifyConstraintForPossibleIntersectionSubType(subType: UnwrappedType, superType: UnwrappedType): Boolean? {
        @Suppress("NAME_SHADOWING")
        val subType = subType.lowerIfFlexible()

        if (!subType.isIntersectionType) return null

        assert(!subType.isMarkedNullable) { "Intersection type should not be marked nullable!: $subType" }

        // TODO: may be we lose flexibility here
        val subIntersectionTypes = (subType.constructor as IntersectionTypeConstructor).supertypes.map { it.lowerIfFlexible() }

        val typeVariables = subIntersectionTypes.filter(this::isMyTypeVariable).check { it.isNotEmpty() } ?: return null
        val notTypeVariables = subIntersectionTypes.filterNot(this::isMyTypeVariable)

        // todo: may be we can do better then that.
        if (notTypeVariables.isNotEmpty() && NewKotlinTypeChecker.isSubtypeOf(intersectTypes(notTypeVariables), superType)) {
            return true
        }

        return typeVariables.all { simplifyUpperConstraint(it, superType) }
    }

    private fun isSubtypeOfByTypeChecker(subType: UnwrappedType, superType: UnwrappedType) =
            with(NewKotlinTypeChecker) { this@TypeCheckerContextForConstraintSystem.isSubtypeOf(subType, superType) }

    private fun assertInputTypes(subType: UnwrappedType, superType: UnwrappedType) {
        fun correctSubType(subType: SimpleType) = subType.isSingleClassifierType || subType.isIntersectionType
        fun correctSuperType(superType: SimpleType) = superType.isSingleClassifierType

        assert(subType.bothBounds(::correctSubType)) { "Not singleClassifierType and not intersection subType: $subType" }
        assert(superType.bothBounds(::correctSuperType)) { "Not singleClassifierType superType: $superType" }
    }

    private inline fun UnwrappedType.bothBounds(f: (SimpleType) -> Boolean) = when (this) {
        is SimpleType -> f(this)
        is FlexibleType -> f(lowerBound) && f(upperBound)
    }

    private inline fun UnwrappedType.anyBound(f: (SimpleType) -> Boolean) = when (this) {
        is SimpleType -> f(this)
        is FlexibleType -> f(lowerBound) || f(upperBound)
    }
}
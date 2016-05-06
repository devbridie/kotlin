/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.jetbrains.uast.java

import com.intellij.psi.*
import org.jetbrains.uast.*
import org.jetbrains.uast.kinds.UastVariance
import org.jetbrains.uast.UTypeProjection


class JavaUType(val psi: PsiType?) : JavaAbstractUElement(), UType {
    override val name: String
        get() = when (psi) {
            is PsiClassType -> psi.className.substringAfterLast('.')
            is PsiArrayType -> "Array"
            else -> psi?.canonicalText?.substringAfterLast('.')
        }.orAnonymous("type")

    override val fqName: String?
        get() = when (psi) {
            is PsiClassType -> psi.resolve()?.qualifiedName
            else -> null
        }

    override val isInt: Boolean
        get() = check(PsiType.INT, "java.lang.Integer")

    override val isLong: Boolean
        get() = check(PsiType.LONG, "java.lang.Long")

    override val isShort: Boolean
        get() = check(PsiType.SHORT, "java.lang.Short")

    override val isFloat: Boolean
        get() = check(PsiType.FLOAT, "java.lang.Float")

    override val isDouble: Boolean
        get() = check(PsiType.DOUBLE, "java.lang.Double")

    override val isChar: Boolean
        get() = check(PsiType.CHAR, "java.lang.Character")

    override val isBoolean: Boolean
        get() = check(PsiType.BOOLEAN, "java.lang.Boolean")

    override val isByte: Boolean
        get() = check(PsiType.BYTE, "java.lang.Byte")

    override val isVoid: Boolean
        get() = check(PsiType.VOID, "java.lang.Byte")

    override val isPrimitiveType: Boolean
        get() = psi is PsiPrimitiveType

    override val isString: Boolean
        get() = (psi as? PsiClassType)?.resolve()?.qualifiedName == "java.lang.String"

    override val isObject: Boolean
        get() = (psi as? PsiClassType)?.resolve()?.qualifiedName == "java.lang.Object"

    override val arguments by lz {
        val classType = psi as? PsiClassType ?: return@lz emptyList<UTypeProjection>()
        if (!classType.hasParameters()) return@lz emptyList<UTypeProjection>()
        classType.parameters.map {
            val type = JavaConverter.convertType(it)
            val variance = when (it) {
                is PsiWildcardType -> {
                    if (it.isSuper)
                        UastVariance.CONTRAVARIANT
                    else if (it.isExtends)
                        UastVariance.CONTRAVARIANT
                    else
                        UastVariance.UNKNOWN
                }
                else -> UastVariance.INVARIANT
            }
            object : UTypeProjection {
                override val type = type
                override val variance = variance
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun check(primitiveType: PsiPrimitiveType, boxedType: String): Boolean =
            psi == primitiveType || (psi as? PsiClassType)?.resolve()?.qualifiedName == boxedType

    override val annotations by lz { psi.getAnnotations(this) }

    override fun resolve(context: UastContext) = when (psi) {
        is PsiClassType -> psi.resolve()?.let { context.convert(it) as? UClass }
        else -> null
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        if (!super.equals(other)) return false

        other as JavaUType

        if (psi != other.psi) return false
        return true
    }
}

class JavaUArrayType(val type: PsiArrayType) : UArrayType {
    override val name: String
        get() = "Array"
    override val fqName: String
        get() = "Array"

    override val isInt: Boolean
        get() = false
    override val isShort: Boolean
        get() = false
    override val isLong: Boolean
        get() = false
    override val isFloat: Boolean
        get() = false
    override val isDouble: Boolean
        get() = false
    override val isChar: Boolean
        get() = false
    override val isBoolean: Boolean
        get() = false
    override val isByte: Boolean
        get() = false
    override val isString: Boolean
        get() = false
    override val isObject: Boolean
        get() = false
    override val isVoid: Boolean
        get() = false
    override val isPrimitiveType: Boolean
        get() = false

    override val arrayElementType by lz { JavaConverter.convertType(type.componentType) }

    override val arguments: List<UTypeProjection> by lz {
        val type = JavaConverter.convertType(type.componentType)
        val typeProjection = object : UTypeProjection {
            override val type = type
            override val variance: UastVariance
                get() = UastVariance.INVARIANT
        }
        listOf(typeProjection)
    }

    override fun resolve(context: UastContext) = null

    override val annotations: List<UAnnotation>
        get() = emptyList()
}
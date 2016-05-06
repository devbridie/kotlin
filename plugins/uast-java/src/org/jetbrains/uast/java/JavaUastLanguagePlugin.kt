/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
import org.jetbrains.uast.java.declarations.JavaPrimitiveUType
import org.jetbrains.uast.java.expressions.JavaUSynchronizedExpression


object JavaUastLanguagePlugin : UastLanguagePlugin {
    override val converter: UastConverter = JavaConverter
    override val visitorExtensions = emptyList<UastVisitorExtension>()
}

internal object JavaConverter : UastConverter {
    override val priority = 0

    override fun isFileSupported(name: String): Boolean {
        return name.endsWith(".java", ignoreCase = true)
    }

    fun convert(file: PsiJavaFile): UFile = JavaUFile(file)

    override fun convert(element: Any?, parent: UElement?): UElement? {
        if (element !is PsiElement) return null
        return convertPsiElement(element, parent)
    }

    override fun convertWithParent(element: Any?): UElement? {
        if (element is PsiJavaFile) return JavaUFile(element)
        if (element is PsiType) return convertType(element)

        if (element !is PsiElement) return null

        val parent = element.parent ?: return null
        val parentUElement = convertWithParent(parent) ?: return null
        return convertPsiElement(element, parentUElement)
    }

    override fun convertWithoutParent(element: Any?): UElement? {
        if (element is PsiJavaFile) return JavaUFile(element)
        if (element is PsiType) return convertType(element)

        if (element !is PsiElement) return null
        return convertPsiElement(element, null)
    }

    private fun convertPsiElement(element: PsiElement?, parent: UElement?) = when (element) {
        is PsiJavaFile -> JavaUFile(element)
        is PsiClass -> JavaUClass(element, parent)
        is PsiCodeBlock -> convertBlock(element, parent)
        is PsiMethod -> convertMethod(element, parent)
        is PsiField -> convertField(element, parent)
        is PsiVariable -> convertVariable(element, parent)
        is PsiClassInitializer -> convertInitializer(element, parent)
        is PsiAnnotation -> convertAnnotation(element, parent)
        is PsiResourceExpression -> convert(element.expression, parent)
        is PsiExpression -> convert(element, parent)
        is PsiStatement -> convert(element, parent)
        is PsiIdentifier -> JavaUSimpleReferenceExpression(element, element.text, parent)
        is PsiImportStatementBase -> convertImportStatement(element, parent)
        is PsiParameter -> convertParameter(element, parent)
        is PsiTypeParameter -> convertTypeParameter(element, parent)
        is PsiNameValuePair -> convertNameValue(element, parent)
        is PsiType -> convertType(element)
        is PsiArrayInitializerMemberValue -> JavaAnnotationArrayInitializerUCallExpression(element, parent)
        else -> null
    }

    internal fun convertImportStatement(importStatement: PsiImportStatementBase, parent: UElement?): UImportStatement? {
        return when (importStatement) {
            is PsiImportStatement -> JavaUImportStatement(importStatement, parent)
            is PsiImportStaticStatement -> JavaUStaticImportStatement(importStatement, parent)
            else -> null
        }
    }

    internal fun convertType(type: PsiType?): UType {
        if (type is PsiPrimitiveType) {
            return JavaPrimitiveUType(type)
        }
        if (type is PsiArrayType) {
            return JavaUArrayType(type)
        }
        return JavaUType(type)
    }

    internal fun convertParameter(parameter: PsiParameter, parent: UElement?) = JavaValueParameterUVariable(parameter, parent)

    internal fun convertBlock(block: PsiCodeBlock, parent: UElement?) = JavaUCodeBlockExpression(block, parent)

    internal fun convertMethod(method: PsiMethod, parent: UElement?) = JavaUFunction(method, parent)

    internal fun convertField(field: PsiField, parent: UElement?) = JavaUVariable(field, parent)

    internal fun convertVariable(variable: PsiVariable, parent: UElement?) = JavaUVariable(variable, parent)

    internal fun convertAnnotation(annotation: PsiAnnotation, parent: UElement?) = JavaUAnnotation(annotation, parent)

    internal fun convertClass(clazz: PsiClass, parent: UElement?) = JavaUClass(clazz, parent)

    internal fun convertInitializer(initializer: PsiClassInitializer, parent: UElement?) = JavaClassInitializerUFunction(initializer, parent)

    internal fun convertTypeParameter(parameter: PsiTypeParameter, parent: UElement?) = JavaParameterUTypeReference(parameter, parent)

    internal fun convertNameValue(pair: PsiNameValuePair, parent: UElement?) = UNamedExpression(pair.name.orAnonymous(), parent).apply {
        val value = pair.value
        expression = convert(value, this) as? UExpression ?: UnknownJavaExpression(value ?: pair, this)
    }

    internal fun convertReference(expression: PsiReferenceExpression, parent: UElement?): UExpression {
        return if (expression.isQualified) {
            JavaUQualifiedExpression(expression, parent)
        } else {
            val name = expression.referenceName ?: "<error name>"
            JavaUSimpleReferenceExpression(expression, name, parent)
        }
    }

    private fun convertPolyadicExpression(
            expression: PsiPolyadicExpression,
            parent: UElement?,
            i: Int
    ): UExpression {
        return if (i == 1) JavaSeparatedPolyadicUBinaryExpression(expression, parent).apply {
            leftOperand = convert(expression.operands[0], this)
            rightOperand = convert(expression.operands[1], this)
        } else JavaSeparatedPolyadicUBinaryExpression(expression, parent).apply {
            leftOperand = convertPolyadicExpression(expression, parent, i - 1)
            rightOperand = convert(expression.operands[i], this)
        }
    }

    internal fun convert(expression: PsiExpression, parent: UElement?): UExpression = when (expression) {
        is PsiPolyadicExpression -> convertPolyadicExpression(expression, parent, expression.operands.size - 1)
        is PsiAssignmentExpression -> JavaUAssignmentExpression(expression, parent)
        is PsiConditionalExpression -> JavaUTernaryIfExpression(expression, parent)
        is PsiNewExpression -> {
            if (expression.anonymousClass != null) {
                JavaUObjectLiteralExpression(expression, parent)
            } else {
                JavaConstructorUCallExpression(expression, parent)
            }
        }
        is PsiMethodCallExpression -> {
            val qualifier = expression.methodExpression.qualifierExpression
            if (qualifier != null) {
                JavaUCompositeQualifiedExpression(parent).apply {
                    receiver = convert(qualifier, this)
                    selector = JavaUCallExpression(expression, this)
                }
            } else {
                JavaUCallExpression(expression, parent)
            }
        }
        is PsiArrayInitializerExpression -> JavaArrayInitializerUCallExpression(expression, parent)
        is PsiBinaryExpression -> JavaUBinaryExpression(expression, parent)
        is PsiParenthesizedExpression -> JavaUParenthesizedExpression(expression, parent)
        is PsiPrefixExpression -> JavaUPrefixExpression(expression, parent)
        is PsiPostfixExpression -> JavaUPostfixExpression(expression, parent)
        is PsiLiteralExpression -> JavaULiteralExpression(expression, parent)
        is PsiReferenceExpression -> convertReference(expression, parent)
        is PsiThisExpression -> JavaUThisExpression(expression, parent)
        is PsiSuperExpression -> JavaUSuperExpression(expression, parent)
        is PsiInstanceOfExpression -> JavaUInstanceCheckExpression(expression, parent)
        is PsiTypeCastExpression -> JavaUTypeCastExpression(expression, parent)
        is PsiClassObjectAccessExpression -> JavaUClassLiteralExpression(expression, parent)
        is PsiArrayAccessExpression -> JavaUArrayAccessExpression(expression, parent)
        is PsiLambdaExpression -> JavaULambdaExpression(expression, parent)
        is PsiMethodReferenceExpression -> JavaUCallableReferenceExpression(expression, parent)

        else -> UnknownJavaExpression(expression, parent)
    }

    internal fun convert(statement: PsiStatement, parent: UElement?): UExpression = when (statement) {
        is PsiDeclarationStatement -> convertDeclarations(statement.declaredElements, parent)
        is PsiExpressionListStatement -> convertDeclarations(statement.expressionList.expressions, parent)
        is PsiBlockStatement -> JavaUBlockExpression(statement, parent)
        is PsiLabeledStatement -> JavaULabeledExpression(statement, parent)
        is PsiExpressionStatement -> convert(statement.expression, parent)
        is PsiIfStatement -> JavaUIfExpression(statement, parent)
        is PsiSwitchStatement -> JavaUSwitchExpression(statement, parent)
        is PsiSwitchLabelStatement -> {
            if (statement.isDefaultCase)
                DefaultUSwitchClauseExpression(parent)
            else JavaUCaseSwitchClauseExpression(statement, parent)
        }
        is PsiWhileStatement -> JavaUWhileExpression(statement, parent)
        is PsiDoWhileStatement -> JavaUDoWhileExpression(statement, parent)
        is PsiForStatement -> JavaUForExpression(statement, parent)
        is PsiForeachStatement -> JavaUForEachExpression(statement, parent)
        is PsiBreakStatement -> JavaUBreakExpression(statement, parent)
        is PsiContinueStatement -> JavaUContinueExpression(statement, parent)
        is PsiReturnStatement -> JavaUReturnExpression(statement, parent)
        is PsiAssertStatement -> JavaUAssertExpression(statement, parent)
        is PsiThrowStatement -> JavaUThrowExpression(statement, parent)
        is PsiSynchronizedStatement -> JavaUSynchronizedExpression(statement, parent)
        is PsiTryStatement -> JavaUTryExpression(statement, parent)

        else -> UnknownJavaExpression(statement, parent)
    }

    internal fun convertOrEmpty(statement: PsiStatement?, parent: UElement?): UExpression {
        return if (statement != null) convert(statement, parent) else EmptyUExpression(parent)
    }

    internal fun convertOrEmpty(expression: PsiExpression?, parent: UElement?): UExpression {
        return if (expression != null) convert(expression, parent) else EmptyUExpression(parent)
    }

    internal fun convertOrNull(expression: PsiExpression?, parent: UElement?): UExpression? {
        return if (expression != null) convert(expression, parent) else null
    }

    internal fun convertOrEmpty(block: PsiCodeBlock?, parent: UElement?): UExpression {
        return if (block != null) convertBlock(block, parent) else EmptyUExpression(parent)
    }

    private fun convertDeclarations(elements: Array<out PsiElement>, parent: UElement?): SimpleUDeclarationsExpression {
        val uelements = arrayListOf<UElement>()
        return SimpleUDeclarationsExpression(parent, uelements).apply {
            for (element in elements) {
                convert(element, this)?.let { uelements += it }
            }
        }
    }
}
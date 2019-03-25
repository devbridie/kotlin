/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.intentions.getArguments
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe

class ReplaceJavaToStringToIntToString : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return callExpressionVisitor { callExpression ->
            if (callExpression.isToString()) {
                holder.registerProblem(
                    callExpression,
                    "Could be replaced with `int.toString()`",
                    ConvertIntegerToStringQuickFix()
                )

            }
        }
    }
}

class ConvertIntegerToStringQuickFix : LocalQuickFix {
    override fun getName() = "Replace with 'int.toString()'"

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as? KtCallExpression ?: return
        val integerClass = element.parent.children[0]
        val arguments = element.valueArguments
        val integerArg = arguments[0]
        val radixArg = arguments.getOrNull(1)

        element.replace(KtPsiFactory(element).createExpression("toString(${radixArg?.text ?: ""})"))
        integerClass.replace(KtPsiFactory(integerClass).createExpression(integerArg.text))
    }
}


private fun KtCallExpression.isToString(): Boolean {
    val fqName = this?.resolveToCall()?.resultingDescriptor?.fqNameUnsafe?.asString()
    return fqName.equals("java.lang.Integer.toString")
}
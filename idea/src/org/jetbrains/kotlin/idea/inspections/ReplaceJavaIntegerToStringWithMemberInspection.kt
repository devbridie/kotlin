/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe

class ReplaceJavaIntegerToStringWithMemberInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return dotQualifiedExpressionVisitor { callExpression ->
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

private class ConvertIntegerToStringQuickFix : LocalQuickFix {
    override fun getName() = "Replace with 'int.toString()'"

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val dotQualifiedExpression = descriptor.psiElement as? KtDotQualifiedExpression ?: return
        val element = dotQualifiedExpression.selectorExpression as? KtCallExpression ?: return
        val arguments = element.valueArguments
        val integerArg = arguments[0]
        val radixArg = arguments.getOrNull(1)

        dotQualifiedExpression.replace(
            KtPsiFactory(element).createExpressionByPattern(
                "$0.toString($1)",
                integerArg.text,
                radixArg?.text ?: ""
            )
        )
    }
}

private fun KtDotQualifiedExpression.isToString(): Boolean {
    val fqName = this.resolveToCall()?.resultingDescriptor?.fqNameUnsafe?.asString()
    return fqName == "java.lang.Integer.toString"
}

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
import org.jetbrains.kotlin.idea.inspections.collections.isCalling
import org.jetbrains.kotlin.idea.intentions.getArguments
import org.jetbrains.kotlin.idea.intentions.getCallableDescriptor
import org.jetbrains.kotlin.idea.intentions.isSizeOrLength
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*

class ReplaceManualRangeWithIndicesCallsInspection : AbstractPrimitiveRangeToInspection() {
    override fun visitRangeToExpression(expression: KtExpression, holder: ProblemsHolder) {
        if (expression.getArguments()?.second?.deparenthesize()?.isSizeMinusOne() != true) return

        holder.registerProblem(
            expression,
            "'rangeTo or the '..' call should be replaced with .indices call",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            ReplaceManualRangeWithIndicesCallQuickFix()
        )




        /*
        if (expression.getArguments()?.second?.deparenthesize()?.isMinusOne() != true) return

        holder.registerProblem(
            expression,
            "'rangeTo' or the '..' call should be replaced with 'until'",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            ReplaceWithUntilQuickFix()
        )*/
    }
    private fun KtExpression.isSizeMinusOne(): Boolean {
        if (this !is KtBinaryExpression) return false
        if (operationToken != KtTokens.MINUS) return false

        //    isCalling(FqName("kotlin.collections.size"))
        //    val leftValue = right?.
        val leftValue = left?.isSizeOrLength() ?: return false
        val constantValue = right?.constantValueOrNull()
        val rightValue = (constantValue?.value as? Number)?.toInt() ?: return false
        return rightValue == 1 && leftValue
    }

}

class ReplaceManualRangeWithIndicesCallQuickFix: LocalQuickFix {
    override fun getName() = "Replace with indices"

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as KtExpression
        val args = element.getArguments() ?: return
        val first = (args.first as? KtCallExpression)?.calleeExpression ?: return
        element.replace(
            KtPsiFactory(element).createExpressionByPattern(
                "$0.indices",
                first
            )

        )
    }

    /*
    override fun getName() = "Replace with until"

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as KtExpression
        val args = element.getArguments() ?: return
        element.replace(
            KtPsiFactory(element).createExpressionByPattern(
                "$0 until $1",
                args.first ?: return,
                (args.second?.deparenthesize() as? KtBinaryExpression)?.left ?: return
            )
        )
    }*/
}



private fun KtExpression.deparenthesize() = KtPsiUtil.safeDeparenthesize(this)


/*
class ReplaceCollectionCountWithSizeQuickFix : LocalQuickFix {
    override fun getName() = "Replace 'count' with 'size'"

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as KtCallExpression
        element.replace(KtPsiFactory(element).createExpression("size"))
    }
}

private fun KtCallExpression.isCount(): Boolean {
    return this.valueArguments.isEmpty() && isCalling(FqName("kotlin.collections.size"))
}
        */
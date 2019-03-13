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
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.intentions.getArguments
import org.jetbrains.kotlin.idea.intentions.getCallableDescriptor
import org.jetbrains.kotlin.idea.intentions.getLeftMostReceiverExpression
import org.jetbrains.kotlin.idea.intentions.isSizeOrLength
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class ReplaceManualRangeWithIndicesCallsInspection : AbstractKotlinInspection() {


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return expressionVisitor { expression ->
            if (expression !is KtBinaryExpression && expression !is KtDotQualifiedExpression) return@expressionVisitor

            val fqName = expression.getCallableDescriptor()?.fqNameUnsafe?.asString() ?: return@expressionVisitor

            if (!fqName.matches(REGEX_RANGE_TO) && !fqName.matches(REGEX_UNTIL)) return@expressionVisitor

            when {
                fqName.matches(REGEX_RANGE_TO) -> visitRangeToExpression(expression, holder)
                fqName.matches(REGEX_UNTIL) -> visitUntilExpression(expression, holder)
                else -> return@expressionVisitor
            }

        }
    }

    private fun visitRangeToExpression(expression: KtExpression, holder: ProblemsHolder) {
        if (expression.getArguments()?.second?.deparenthesize()?.isSizeMinusOne() != true) return

        val constantValue = expression.getArguments()?.first?.constantValueOrNull()
        val rightValue = (constantValue?.value as? Number)?.toInt() ?: return
        if (rightValue != 0) return


        holder.registerProblem(
            expression,
            "'rangeTo or the '..' call should be replaced with .indices call",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            ReplaceManualRangeWithIndicesCallQuickFix()
        )

    }

    private fun visitUntilExpression(expression: KtExpression, holder: ProblemsHolder) {
        if (expression.getArguments()?.second?.deparenthesize()?.isSizeOrLength() != true) return

        val constantValue = expression.getArguments()?.first?.constantValueOrNull()
        val rightValue = (constantValue?.value as? Number)?.toInt() ?: return
        if (rightValue != 0) return


        holder.registerProblem(
            expression,
            "'rangeTo or the '..' call should be replaced with .indices call",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            ReplaceManualRangeWithIndicesCallQuickFix()
        )

    }


    private fun KtExpression.isSizeMinusOne(): Boolean {
        if (this !is KtBinaryExpression) return false
        if (operationToken != KtTokens.MINUS) return false

        val leftValue = left?.isSizeOrLength() ?: return false
        val constantValue = right?.constantValueOrNull()
        val rightValue = (constantValue?.value as? Number)?.toInt() ?: return false
        return rightValue == 1 && leftValue
    }

}

class ReplaceManualRangeWithIndicesCallQuickFix : LocalQuickFix {
    override fun getName() = "Replace with indices"

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as KtExpression
        val args = element.getArguments() ?: return
        val second = (args.second as? KtBinaryExpression) ?: return
        val array = (second.left as? KtDotQualifiedExpression)?.getLeftMostReceiverExpression() ?: return

        element.replace(
            KtPsiFactory(element).createExpressionByPattern(
                "$0.indices",
                array
            )

        )
    }

}


private fun KtExpression.deparenthesize() = KtPsiUtil.safeDeparenthesize(this)

private val REGEX_RANGE_TO = """kotlin.(Char|Byte|Short|Int|Long).rangeTo""".toRegex()
private val REGEX_UNTIL = """kotlin.(Char|Byte|Short|Int|Long).until""".toRegex()


fun KtExpression.constantValueOrNull(context: BindingContext? = null): ConstantValue<Any?>? {
    val c = context ?: this.analyze(BodyResolveMode.PARTIAL)

    val constant = ConstantExpressionEvaluator.getConstant(this, c) ?: return null

    return constant.toConstantValue(getType(c) ?: return null)
}


/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.search.searches.ReferencesSearch
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.findUsages.processAllUsages
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.synthetics.findClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

class ConvertToObjectDeclarationInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return classVisitor { clazz ->
            val descriptor = clazz.findClassDescriptor(clazz.analyze())
            if (descriptor.getSuperClassNotAny() != null || descriptor.getSuperInterfaces().isNotEmpty()) return@classVisitor
            val declarations = clazz.declarations
            if (declarations.size == 1) {
                val firstDecl = declarations[0] as? KtObjectDeclaration ?: return@classVisitor
                if (firstDecl.isCompanion() && firstDecl.nameIdentifier == null) {
                    holder.registerProblem(
                        clazz,
                        "Can be converted to object declaration",
                        ConvertToObjectDeclarationFix()
                    )
                }
            }
        }
    }

    private class ConvertToObjectDeclarationFix : LocalQuickFix {
        override fun getFamilyName() = "Convert to object declaration"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val clazz = descriptor.psiElement as? KtClass ?: return
            val obj = clazz.declarations[0] as? KtObjectDeclaration ?: return
            removeCompanionReference(clazz)

            obj.setName(clazz.name!!)
            obj.removeModifier(KtTokens.COMPANION_KEYWORD)
            clazz.replace(obj)
        }

        private fun removeCompanionReference(clazz: KtClass) {
            val currentCompanion = clazz.companionObjects.first()
            ReferencesSearch.search(currentCompanion).forEach { ref ->
                val dotQualified = ref.element.parent as? KtDotQualifiedExpression ?: return@forEach
                if (dotQualified.selectorExpression?.text == "Companion") {
                    dotQualified.replace(dotQualified.receiverExpression)
                }
            }
        }
    }
}

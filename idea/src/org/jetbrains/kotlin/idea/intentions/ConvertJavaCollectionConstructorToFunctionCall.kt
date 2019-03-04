/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.intentions

import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

class ConvertJavaCollectionConstructorToFunctionCall : SelfTargetingIntention<KtNameReferenceExpression>(
    KtNameReferenceExpression::class.java, "Convert Java collection call to function call"
) {
    override fun isApplicableTo(element: KtNameReferenceExpression, caretOffset: Int): Boolean {

        val name = element.text ?: return false

        return name == "ArrayList"
    }

    override fun applyTo(element: KtNameReferenceExpression, editor: Editor?) {

        val arrayListOfCall = KtPsiFactory(element).createIdentifier("arrayListOf")

        element.replace(arrayListOfCall)
    }
}

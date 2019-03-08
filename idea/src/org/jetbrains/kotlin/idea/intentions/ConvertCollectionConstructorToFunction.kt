/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.intentions

import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

class ConvertCollectionConstructorToFunction : SelfTargetingIntention<KtNameReferenceExpression>(
    KtNameReferenceExpression::class.java, "Convert Java collection call to function call"
) {

    private val functionMap = hashMapOf(
        "ArrayList" to "arrayListOf",
        "HashMap" to "hashMapOf",
        "HashSet" to "hashSetOf"
    )

    override fun isApplicableTo(element: KtNameReferenceExpression, caretOffset: Int): Boolean {

        val name = element.text ?: return false

        return functionMap.containsKey(name)
    }

    override fun applyTo(element: KtNameReferenceExpression, editor: Editor?) {

        val name = element.text ?: return

        val functionName = functionMap[name] ?: return

        val arrayListOfCall = KtPsiFactory(element).createIdentifier(functionName)

        element.replace(arrayListOfCall)
    }
}

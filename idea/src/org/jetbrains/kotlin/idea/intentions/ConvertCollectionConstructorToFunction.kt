/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.intentions

import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class ConvertCollectionConstructorToFunction : SelfTargetingIntention<KtCallExpression>(
    KtCallExpression::class.java, "Convert Java collection call to function call"
) {

    private val functionMap = hashMapOf(
        "java.util.ArrayList.<init>" to "arrayListOf",
        "java.util.HashMap.<init>" to "hashMapOf",
        "java.util.HashSet.<init>" to "hashSetOf",
        "java.util.LinkedHashMap.<init>" to "linkedMapOf",
        "java.util.LinkedHashSet.<init>" to "linkedSetOf"
    )

    override fun isApplicableTo(element: KtCallExpression, caretOffset: Int): Boolean {
        val fq = element.resolveToCall()?.resultingDescriptor?.fqNameSafe?.asString() ?: return false
        return functionMap.containsKey(fq)
    }

    override fun applyTo(element: KtCallExpression, editor: Editor?) {
        val fq = element.resolveToCall()?.resultingDescriptor?.fqNameSafe?.asString() ?: return
        val toCall = functionMap[fq] ?: return
        val arrayListOfCall = KtPsiFactory(element).createIdentifier(toCall)
        element.calleeExpression?.replace(arrayListOfCall)
    }
}

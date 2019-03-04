/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.intentions

import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.core.setType
import org.jetbrains.kotlin.idea.inspections.collections.isCalling
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.types.typeUtil.makeNullable

class ConvertJavaCollectionConstructorToFunctionCall : SelfTargetingIntention<KtCallExpression>(
    KtCallExpression::class.java, "Convert Java collection call to function call"
) {
    override fun isApplicableTo(element: KtCallExpression, caretOffset: Int): Boolean {

        return element.isCalling(FqName("kotlin.collections.TypeAlias.ArrayList"))
    }

    override fun applyTo(element: KtCallExpression, editor: Editor?) {
        /*val typeReference: KtTypeReference = element.typeReference ?: return
        val nullableType = element.analyze(BodyResolveMode.PARTIAL)[BindingContext.TYPE, typeReference]?.makeNullable() ?: return
        element.removeModifier(KtTokens.LATEINIT_KEYWORD)
        element.setType(nullableType)
        element.initializer = KtPsiFactory(element).createExpression(KtTokens.NULL_KEYWORD.value)*/
        element.replace(KtPsiFactory(element).createExpression("arrayListOf()"))
    }
}

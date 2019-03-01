/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.inspections

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class ReplaceManualRangeWithIndecesCalls : AbstractKotlinInspection() {
    override fun buildVisitor(holder: Problemsholder, isOnTheFly: Boolean): PsiElementVisitor {

    }
}
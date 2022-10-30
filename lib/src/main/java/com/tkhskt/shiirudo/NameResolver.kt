package com.tkhskt.shiirudo

import com.google.devtools.ksp.symbol.KSDeclaration

internal class NameResolver {
    companion object {
        fun createPropertyName(
            rootDeclaration: KSDeclaration?,
            classDeclaration: KSDeclaration,
            includeRoot: Boolean = false,
            reverse: Boolean = false,
            currentName: String = classDeclaration.simpleName.asString(),
        ): String {
            val parent = classDeclaration.parentDeclaration ?: return currentName
            val name = if (reverse) {
                "$currentName${parent.simpleName.asString()}"
            } else {
                "${parent.simpleName.asString()}$currentName"
            }
            if (parent == rootDeclaration && includeRoot) {
                return name
            } else if (parent == rootDeclaration) {
                return currentName
            }
            return createPropertyName(
                rootDeclaration = rootDeclaration,
                classDeclaration = parent,
                currentName = name,
                includeRoot = includeRoot
            )
        }
    }
}

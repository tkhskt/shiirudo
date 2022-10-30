package com.tkhskt.shiirudo.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

internal class ShiirudoTargetValidator {
    companion object {
        fun validate(
            classDeclaration: KSClassDeclaration,
            logger: KSPLogger,
        ) {
            if (!classDeclaration.isSealed()) {
                logger.error("${classDeclaration.simpleName.asString()}: Shiirudo annotation can only be applied to sealed class or sealed interface.")
            }
            if (!classDeclaration.hasSubclasses()) {
                logger.error("${classDeclaration.simpleName.asString()} has no subclasses.")
            }
        }

        private fun KSClassDeclaration.isSealed(): Boolean {
            return modifiers.contains(Modifier.SEALED)
        }

        private fun KSClassDeclaration.hasSubclasses(): Boolean {
            return getSealedSubclasses().toList().isNotEmpty()
        }
    }
}

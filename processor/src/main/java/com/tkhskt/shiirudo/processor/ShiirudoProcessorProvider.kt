package com.tkhskt.shiirudo.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ShiirudoProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ShiirudoProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
    }
}

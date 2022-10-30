package com.tkhskt.shiirudo

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.tkhskt.shiirudo.ClassNames.SHIIRUDO
import com.tkhskt.shiirudo.generator.builder.ShiirudoBuilderGenerator
import com.tkhskt.shiirudo.generator.builder.ShiirudoDslGenerator
import com.tkhskt.shiirudo.generator.builder.ShiirudoGenerator
import com.tkhskt.shiirudo.generator.executor.ShiirudoExecutorDslGenerator
import com.tkhskt.shiirudo.generator.executor.ShiirudoExecutorGenerator
import com.tkhskt.shiirudo.validator.ShiirudoTargetValidator

class ShiirudoProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            resolver.getSymbolsWithAnnotation(SHIIRUDO.canonicalName)
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(ShiirudoVisitor(), Unit) }
        return ret
    }

    inner class ShiirudoVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            ShiirudoTargetValidator.validate(classDeclaration, logger)

            val shiirudoGenerator =
                ShiirudoGenerator(codeGenerator, logger)
            shiirudoGenerator.generate(classDeclaration)
            val shiirudoBuilderGenerator =
                ShiirudoBuilderGenerator(codeGenerator, logger)
            shiirudoBuilderGenerator.generate(classDeclaration)
            val shiirudoDslGenerator =
                ShiirudoDslGenerator(codeGenerator, logger)
            shiirudoDslGenerator.generate(classDeclaration)
            val shiirudoExecutorGenerator =
                ShiirudoExecutorGenerator(codeGenerator, logger)
            shiirudoExecutorGenerator.generate(classDeclaration)
            val shiirudoExecutorDslGenerator =
                ShiirudoExecutorDslGenerator(codeGenerator, logger)
            shiirudoExecutorDslGenerator.generate(classDeclaration)
        }
    }
}

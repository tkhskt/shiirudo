package com.tkhskt.shiirudo.generator.executor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class ShiirudoExecutorDslGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    private lateinit var annotatedClassDeclaration: KSClassDeclaration
    private lateinit var annotatedClassName: ClassName
    private lateinit var subclasses: Sequence<KSClassDeclaration>

    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        annotatedClassDeclaration = classDeclaration
        annotatedClassName = classDeclaration.toClassName()
        subclasses = classDeclaration.getSealedSubclasses()
        generateHandler(
            packageName = packageName,
            containingFile = classDeclaration.containingFile!!
        )
    }

    private fun generateHandler(
        packageName: String,
        containingFile: KSFile,
    ) {
        val namePrefix = ShiirudoExecutorGenerator.getExecutorClassName(annotatedClassDeclaration)
        val fileName = "${namePrefix.simpleName}Extension"
        val file = FileSpec
            .builder(packageName, fileName)
            .addExecuteFunction()
            .addLambdaExecuteFunction()
            .build()
        file.writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private fun FileSpec.Builder.addExecuteFunction(): FileSpec.Builder {
        val executorClassName =
            ShiirudoExecutorGenerator.getExecutorClassName(annotatedClassDeclaration)
        addFunction(
            FunSpec.builder("shiirudo")
                .returns(executorClassName)
                .receiver(annotatedClassName)
                .addCode(
                    """
                    |return ${executorClassName.simpleName}(this)
                    """.trimMargin()
                )
                .build()
        )
        return this
    }

    private fun FileSpec.Builder.addLambdaExecuteFunction(): FileSpec.Builder {
        val executorClassName =
            ShiirudoExecutorGenerator.getExecutorClassName(annotatedClassDeclaration)
        val lambdaTypeName = LambdaTypeName.get(
            receiver = null,
            returnType = annotatedClassName
        )
        addFunction(
            FunSpec.builder("shiirudo")
                .returns(executorClassName)
                .addParameter("block", lambdaTypeName)
                .addCode(
                    """
                    |return ${executorClassName.simpleName}(block.invoke())
                    """.trimMargin()
                )
                .build()
        )
        return this
    }
}

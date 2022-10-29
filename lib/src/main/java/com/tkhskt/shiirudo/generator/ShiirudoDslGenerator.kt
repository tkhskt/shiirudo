package com.tkhskt.shiirudo.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.tkhskt.shiirudo.NameResolver

class ShiirudoDslGenerator(
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
        val namePrefix =
            NameResolver.createPropertyName(
                rootDeclaration = null,
                classDeclaration = annotatedClassDeclaration,
                includeRoot = true
            )
        val fileName = "${namePrefix}Extension"
        val file = FileSpec
            .builder(packageName, fileName)
            .addHandleFunction()
            .build()
        file.writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private fun FileSpec.Builder.addHandleFunction(): FileSpec.Builder {
        val builderClassName =
            ShiirudoBuilderGenerator.getBuilderClassName(annotatedClassDeclaration)
        val parameterTypeName = LambdaTypeName.get(
            receiver = builderClassName,
            returnType = Unit::class.asTypeName(),
        )
        val branches = subclasses.map { subclass ->
            val nameSuffix = NameResolver.createPropertyName(
                rootDeclaration = annotatedClassDeclaration,
                classDeclaration = subclass,
                reverse = true
            )
            subclass.toClassName().canonicalName to nameSuffix
        }.joinToString("\n") {
            """
            |  is ${it.first} -> {
            |    val f = shiirudoClass.on${it.second} ?: shiirudoClass.onElse
            |    f.invoke(this)
            |  }
            """.trimMargin()
        }
        addFunction(
            FunSpec.builder("handle")
                .receiver(annotatedClassName)
                .addParameter("handler", parameterTypeName)
                .addCode(
                    """
                    |val shiirudoClass = ${builderClassName.simpleName}().apply(handler).build()
                    |when(this) {
                    |$branches
                    |}
                    """.trimMargin()
                )
                .build()
        )
        return this
    }
}

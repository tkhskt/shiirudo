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

class ShiirudoDslGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.toString()
        classDeclaration.getSealedSubclasses().forEach {
            logger.info(packageName)
        }

        generateHandler(
            packageName = packageName,
            className = className,
            parentClass = classDeclaration,
            subclasses = classDeclaration.getSealedSubclasses(),
            containingFile = classDeclaration.containingFile!!
        )
    }

    private fun generateHandler(
        packageName: String,
        className: String,
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>,
        containingFile: KSFile,
    ) {
        val fileName = "${className}Extension"
        val file = FileSpec
            .builder(packageName, fileName)
            .addHandleFunction(parentClass, subclasses)
            .build()
        file.writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private fun FileSpec.Builder.addHandleFunction(
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>
    ): FileSpec.Builder {
        val parentClassNameString = parentClass.simpleName.asString()
        val shiirudoClassName = ClassName(
            parentClass.packageName.asString(),
            "${parentClassNameString}ShiirudoBuilder"
        )
        val parameterTypeName = LambdaTypeName.get(
            receiver = shiirudoClassName,
            returnType = Unit::class.asTypeName(),
        )

        val branches = subclasses.map { subclass ->
            subclass.toClassName().canonicalName to subclass.simpleName.asString()
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
                .receiver(parentClass.toClassName())
                .addParameter("handler", parameterTypeName)
                .addCode(
                    """
                    |val shiirudoClass = ${parentClassNameString}ShiirudoBuilder().apply(handler).build()
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

package com.tkhskt.shiirudo.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo

class ShiirudoBuilderGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.toString()
        val properties = classDeclaration.getAllProperties()
        generateShiirudoBuilder(
            packageName,
            className,
            properties,
            classDeclaration.containingFile!!
        )
    }

    private fun generateShiirudoBuilder(
        packageName: String,
        parameterClassName: String,
        properties: Sequence<KSPropertyDeclaration>,
        containingFile: KSFile,
    ) {
        val builderClassName = "${parameterClassName}Builder"
        val file = FileSpec.builder(packageName, builderClassName).build()
        file.writeTo(codeGenerator, Dependencies(false, containingFile))
    }
}

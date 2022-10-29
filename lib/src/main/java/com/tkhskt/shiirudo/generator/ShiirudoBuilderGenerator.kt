package com.tkhskt.shiirudo.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.tkhskt.shiirudo.NameResolver

class ShiirudoBuilderGenerator(
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
        generateShiirudoBuilder(
            packageName = packageName,
            containingFile = classDeclaration.containingFile!!
        )
    }

    private fun generateShiirudoBuilder(
        packageName: String,
        containingFile: KSFile,
    ) {
        val shiirudoClassNamePrefix =
            NameResolver.createPropertyName(
                rootDeclaration = null,
                classDeclaration = annotatedClassDeclaration,
                includeRoot = true
            )
        val shiirudoClassName = "${shiirudoClassNamePrefix}Shiirudo"
        val builderClassName = "${shiirudoClassName}Builder"
        val typeSpec = TypeSpec.classBuilder(builderClassName)
            .addProperties()
            .addFunctions()
            .addBuildFunction()
            .build()
        val file = FileSpec
            .builder(packageName, builderClassName)
            .addType(typeSpec)
            .build()
        file.writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private fun TypeSpec.Builder.addProperties(): TypeSpec.Builder {
        subclasses.forEach { subclass ->
            val nameSuffix = NameResolver.createPropertyName(
                rootDeclaration = annotatedClassDeclaration,
                classDeclaration = subclass,
                reverse = true
            )
            val className = subclass.toClassName()
            val property = createBuilderProperty(
                className = className,
                name = "on$nameSuffix",
            )
            addProperty(property)
        }
        val property = createBuilderProperty(
            className = annotatedClassName,
            name = "onElse",
            nullable = false,
            initializer = "{}"
        )
        addProperty(property)
        return this
    }

    private fun TypeSpec.Builder.addFunctions(): TypeSpec.Builder {
        subclasses.forEach { subclass ->
            val nameSuffix = NameResolver.createPropertyName(
                rootDeclaration = annotatedClassDeclaration,
                classDeclaration = subclass,
                reverse = true
            )
            val className = subclass.toClassName()
            val func = createBuilderFunction(
                className = className,
                name = "on$nameSuffix"
            )
            addFunction(func)
        }
        val func = createBuilderFunction(
            className = annotatedClassName,
            name = "onElse",
        )
        addFunction(func)
        return this
    }

    private fun TypeSpec.Builder.addBuildFunction(): TypeSpec.Builder {
        val constructorStatement = subclasses.map { subclass ->
            NameResolver.createPropertyName(
                rootDeclaration = annotatedClassDeclaration,
                classDeclaration = subclass,
                reverse = true
            )
        }.joinToString(
            ",\n"
        ) { subclassName ->
            "  on$subclassName = this.on$subclassName"
        }
        val shiirudoClassNamePrefix =
            NameResolver.createPropertyName(
                rootDeclaration = null,
                classDeclaration = annotatedClassDeclaration,
                includeRoot = true
            )
        val shiirudoClassName = "${shiirudoClassNamePrefix}Shiirudo"
        val func = FunSpec.builder("build")
            .returns(ClassName(annotatedClassDeclaration.packageName.asString(), shiirudoClassName))
            .addStatement(
                "return ${shiirudoClassName}(\n$constructorStatement,\n  onElse = this.onElse\n)"
            )
            .build()
        addFunction(func)
        return this
    }

    private fun createBuilderProperty(
        className: ClassName,
        name: String,
        nullable: Boolean = true,
        initializer: String = "null"
    ): PropertySpec {
        val parameterSpec = ParameterSpec.builder("", className).build()
        val lambdaTypeSpec = LambdaTypeName.get(
            receiver = null,
            parameters = listOf(parameterSpec),
            returnType = Unit::class.asClassName()
        ).copy(nullable = nullable)
        return PropertySpec.builder(
            name = name,
            type = lambdaTypeSpec,
            modifiers = listOf(KModifier.PRIVATE)
        ).mutable().initializer(initializer).build()
    }

    private fun createBuilderFunction(
        className: ClassName,
        name: String,
    ): FunSpec {
        val parameterSpec = ParameterSpec.builder("", className).build()
        val lambdaTypeSpec = LambdaTypeName.get(
            receiver = null,
            parameters = listOf(parameterSpec),
            returnType = Unit::class.asClassName()
        )
        return FunSpec.builder(name)
            .addParameter(
                name = "f",
                lambdaTypeSpec,
            ).addStatement(
                "this.$name = f"
            )
            .build()
    }
}

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
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.tkhskt.shiirudo.NameResolver

class ShiirudoGenerator(
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
        generateShiirudo(
            packageName = packageName,
            containingFile = classDeclaration.containingFile!!
        )
    }

    private fun generateShiirudo(
        packageName: String,
        containingFile: KSFile,
    ) {
        val shiirudoClassNameString = getShiirudoClassName(annotatedClassDeclaration).simpleName
        val constructorFunSpec = FunSpec.constructorBuilder()
            .addParameters()
            .build()
        val constructorTypeSpec = TypeSpec.classBuilder(shiirudoClassNameString)
            .primaryConstructor(constructorFunSpec)
            .addProperties()
            .build()
        val file = FileSpec
            .builder(packageName, shiirudoClassNameString)
            .addType(constructorTypeSpec)
            .build()
        file.writeTo(codeGenerator, Dependencies(true, containingFile))
    }

    private fun TypeSpec.Builder.addProperties(): TypeSpec.Builder {

        subclasses.forEach { subclass ->
            val nameSuffix = NameResolver.createPropertyName(
                rootDeclaration = annotatedClassDeclaration,
                classDeclaration = subclass,
                reverse = true
            )
            val className = subclass.toClassName()
            val property = createProperty(
                className = className,
                name = "is$nameSuffix"
            )
            addProperty(property)
        }
        val property = createProperty(
            className = annotatedClassName,
            name = "isElse",
            nullable = false,
        )
        addProperty(property)
        return this
    }

    private fun FunSpec.Builder.addParameters(): FunSpec.Builder {
        subclasses.forEach { subclass ->
            val nameSuffix = NameResolver.createPropertyName(
                rootDeclaration = annotatedClassDeclaration,
                classDeclaration = subclass,
                reverse = true
            )
            val parameter = createParameter(
                className = subclass.toClassName(),
                name = "is$nameSuffix"
            )
            addParameter(parameter)
        }
        val parameter = createParameter(
            className = annotatedClassName,
            name = "isElse",
            nullable = false,
        )
        addParameter(parameter)
        return this
    }

    private fun createParameter(
        className: ClassName,
        name: String,
        nullable: Boolean = true,
    ): ParameterSpec {
        val parameterSpec = ParameterSpec.builder("", className).build()
        val lambdaTypeSpec = LambdaTypeName.get(
            receiver = null,
            parameters = listOf(parameterSpec),
            returnType = Unit::class.asClassName()
        ).copy(nullable = nullable)
        return ParameterSpec.builder(
            name = name,
            type = lambdaTypeSpec,
        ).build()
    }

    private fun createProperty(
        className: ClassName,
        name: String,
        nullable: Boolean = true,
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
        ).initializer(name).build()
    }

    companion object {
        fun getShiirudoClassName(annotatedKSClassDeclaration: KSClassDeclaration): ClassName {
            val namePrefix =
                NameResolver.createPropertyName(
                    rootDeclaration = null,
                    classDeclaration = annotatedKSClassDeclaration,
                    includeRoot = true
                )
            return ClassName(annotatedKSClassDeclaration.packageName.asString(), "${namePrefix}Shiirudo")
        }
    }
}

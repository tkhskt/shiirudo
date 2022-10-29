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

class ShiirudoGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.toString()
        generateShiirudo(
            packageName = packageName,
            className = className,
            parentClass = classDeclaration,
            subclasses = classDeclaration.getSealedSubclasses(),
            containingFile = classDeclaration.containingFile!!
        )
    }

    private fun generateShiirudo(
        packageName: String,
        className: String,
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>,
        containingFile: KSFile,
    ) {
        val shiirudoClassName = "${className}Shiirudo"
        val classBuilder = TypeSpec.classBuilder(shiirudoClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(parentClass, subclasses)
                    .build()
            ).addProperties(parentClass, subclasses)

        val file = FileSpec
            .builder(packageName, shiirudoClassName)
            .addType(classBuilder.build())
            .build()
        file.writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private fun TypeSpec.Builder.addProperties(
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>
    ): TypeSpec.Builder {
        subclasses.forEach { subclass ->
            val className = subclass.toClassName()
            val property = createProperty(
                className = className,
                name = "on${className.simpleName}"
            )
            addProperty(property)
        }
        val className = parentClass.toClassName()
        val property = createProperty(
            className = className,
            name = "onElse",
            nullable = false,
        )
        addProperty(property)
        return this
    }

    private fun FunSpec.Builder.addParameters(
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>
    ): FunSpec.Builder {
        subclasses.forEach { subclass ->
            val className = subclass.toClassName()
            val parameter = createParameter(
                className = className,
                name = "on${className.simpleName}"
            )
            addParameter(parameter)
        }
        val className = parentClass.toClassName()
        val parameter = createParameter(
            className = className,
            name = "onElse",
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
}

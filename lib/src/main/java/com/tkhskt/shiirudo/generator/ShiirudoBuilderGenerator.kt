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

class ShiirudoBuilderGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.toString()
        classDeclaration.getSealedSubclasses().forEach {
            logger.info(packageName)
        }

        generateShiirudoBuilder(
            packageName = packageName,
            className = className,
            parentClass = classDeclaration,
            subclasses = classDeclaration.getSealedSubclasses(),
            containingFile = classDeclaration.containingFile!!
        )
    }

    private fun generateShiirudoBuilder(
        packageName: String,
        className: String,
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>,
        containingFile: KSFile,
    ) {
        val builderClassName = "${className}ShiirudoBuilder"
        val file = FileSpec
            .builder(packageName, builderClassName)
            .addType(
                TypeSpec.classBuilder(builderClassName)
                    .addProperties(parentClass, subclasses)
                    .addFunctions(parentClass, subclasses)
                    .addBuildFunction(parentClass, subclasses)
                    .build()
            ).build()
        file.writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private fun TypeSpec.Builder.addProperties(
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>
    ): TypeSpec.Builder {
        subclasses.forEach { subclass ->
            val className = subclass.toClassName()
            val property = createBuilderProperty(
                className = className,
                name = "on${className.simpleName}",
            )
            addProperty(property)
        }
        val className = parentClass.toClassName()
        val property = createBuilderProperty(
            className = className,
            name = "onElse",
            nullable = false,
            initializer = "{}"
        )
        addProperty(property)
        return this
    }

    private fun TypeSpec.Builder.addFunctions(
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>
    ): TypeSpec.Builder {
        subclasses.forEach { subclass ->
            val className = subclass.toClassName()
            val func = createBuilderFunction(
                className = className,
                name = "on${className.simpleName}"
            )
            addFunction(func)
        }
        val className = parentClass.toClassName()
        val func = createBuilderFunction(
            className = className,
            name = "onElse",
        )
        addFunction(func)
        return this
    }

    private fun TypeSpec.Builder.addBuildFunction(
        parentClass: KSClassDeclaration,
        subclasses: Sequence<KSClassDeclaration>
    ): TypeSpec.Builder {
        val constructorStatement = subclasses.map {
            it.simpleName.asString()
        }.joinToString(
            ",\n"
        ) { subclassName ->
            "  on$subclassName = this.on$subclassName"
        }
        val func = FunSpec.builder("build")
            .returns(ClassName(parentClass.packageName.asString(), "${parentClass}Shiirudo"))
            .addStatement(
                "return ${parentClass.simpleName.asString()}Shiirudo(\n$constructorStatement,\n  onElse = this.onElse\n)"
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

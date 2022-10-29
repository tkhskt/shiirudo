package com.tkhskt.shiirudo.generator.executor

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

class ShiirudoExecutorGenerator(
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
        generateShiirudoExecutor(
            packageName = packageName,
            containingFile = classDeclaration.containingFile!!
        )
    }

    private fun generateShiirudoExecutor(
        packageName: String,
        containingFile: KSFile,
    ) {
        val executorClassNameString = getExecutorClassName(annotatedClassDeclaration)
        val constructorFunSpec = FunSpec.constructorBuilder()
            .addConstructorParameters()
            .build()
        val typeSpec = TypeSpec.classBuilder(executorClassNameString)
            .primaryConstructor(constructorFunSpec)
            .addConstructorProperties()
            .addProperties()
            .addFunctions()
            .addExecuteFunction()
            .build()
        val file = FileSpec
            .builder(packageName, executorClassNameString.simpleName)
            .addType(typeSpec)
            .build()
        file.writeTo(codeGenerator, Dependencies(false, containingFile))
    }

    private fun FunSpec.Builder.addConstructorParameters(): FunSpec.Builder {
        val parameter = ParameterSpec.builder(
            name = "event",
            type = annotatedClassName,
        ).build()
        addParameter(parameter)
        return this
    }

    private fun TypeSpec.Builder.addConstructorProperties(): TypeSpec.Builder {
        val property = PropertySpec.builder(
            name = "event",
            type = annotatedClassName,
            modifiers = listOf(KModifier.PRIVATE)
        ).initializer("event").build()
        addProperty(property)
        return this
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
                name = "is$nameSuffix",
            )
            addProperty(property)
        }
        val property = createProperty(
            className = annotatedClassName,
            name = "isElse",
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
            val func = createFunction(
                className = className,
                name = "is$nameSuffix",
            )
            addFunction(func)
        }
        val func = createFunction(
            className = annotatedClassName,
            name = "isElse",
        )
        addFunction(func)
        return this
    }

    private fun TypeSpec.Builder.addExecuteFunction(): TypeSpec.Builder {
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
            |    val f = this.is${it.second} ?: this.isElse
            |    f.invoke(event)
            |  }
            """.trimMargin()
        }
        addFunction(
            FunSpec.builder("execute")
                .addCode(
                    """
                    |when(event) {
                    |$branches
                    |}
                    """.trimMargin()
                )
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        return this
    }

    private fun createProperty(
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

    private fun createFunction(
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
            .returns(getExecutorClassName(annotatedClassDeclaration))
            .addParameter(
                name = "f",
                lambdaTypeSpec,
            ).addCode(
                """
                |this.$name = f
                |execute()
                |return this
                """.trimMargin()
            )
            .build()
    }

    companion object {
        fun getExecutorClassName(annotatedKSClassDeclaration: KSClassDeclaration): ClassName {
            val shiirudoClassNamePrefix =
                NameResolver.createPropertyName(
                    rootDeclaration = null,
                    classDeclaration = annotatedKSClassDeclaration,
                    includeRoot = true
                )
            return ClassName(
                annotatedKSClassDeclaration.packageName.asString(),
                "${shiirudoClassNamePrefix}ShiirudoExecutor"
            )
        }
    }
}

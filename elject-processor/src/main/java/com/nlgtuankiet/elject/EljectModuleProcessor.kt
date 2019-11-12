package com.nlgtuankiet.elject

import com.google.auto.service.AutoService
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import dagger.Module
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
class EljectModuleProcessor : AbstractProcessor() {
    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var elements: Elements
    private lateinit var types: Types

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        env.options
        messager = env.messager
        filer = env.filer
        elements = env.elementUtils
        types = env.typeUtils
        filer = env.filer
    }

    override fun process(
        p0: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        val eljectModuleClassNames = roundEnvironment.getElementsAnnotatedWith(Elject::class.java)
            .map { it as TypeElement }
            .map { element ->
                val packages = elements.getPackageOf(element)
                val packageName = packages.qualifiedName.toString()
                val clazzName = element.qualifiedName
                    .substring(packageName.length + 1, element.qualifiedName.length)
                    .replace(".", "_")
                "${packageName}.${clazzName}"
            }
        roundEnvironment.getElementsAnnotatedWith(EljectModule::class.java).forEach {
            generateModuleWith(it as TypeElement, eljectModuleClassNames)
        }

        return false
    }

    fun generateModuleWith(element: TypeElement, eljectModuleClassNames: List<String>) {
        val packages = elements.getPackageOf(element)
        val packageName = packages.qualifiedName.toString()
        val clazzName = element.qualifiedName
            .substring(packageName.length + 1, element.qualifiedName.length)
            .replace(".", "_")
        val includePart = eljectModuleClassNames.map {
            "${it}EljectModule.class,\n"
        }.joinToString("")
        val moduleType = TypeSpec.classBuilder("${clazzName}EljectModule")
            .addAnnotation(
                AnnotationSpec.builder(Module::class.java)
                    .addMember(
                        "includes",
                        CodeBlock.of(
                            buildString {
                                append("{\n")
                                append(includePart)
                                append("}")
                            }
                        )
                    )
                    .build()
            )
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .build()

        JavaFile.builder(packages.qualifiedName.toString(), moduleType)
            .build()
            .writeTo(filer)
    }

    private fun warn(message: String) {
        messager.printMessage(Diagnostic.Kind.WARNING, message)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            EljectModule::class.java.canonicalName,
            Elject::class.java.canonicalName
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }
}
package com.nlgtuankiet.elject

import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import dagger.Module
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor::class)
class EljectProcessor : AbstractProcessor() {
    val contributeClazz = Class.forName("dagger.android.ContributesAndroidInjector")

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
        roundEnvironment.getElementsAnnotatedWith(Elject::class.java).forEach {
            generateModuleWith(it as TypeElement)
        }

        return false
    }

    fun generateModuleWith(element: TypeElement) {
        val packages = elements.getPackageOf(element)
        val packageName = packages.qualifiedName.toString()
        val clazzName = element.qualifiedName
            .substring(packageName.length + 1, element.qualifiedName.length)
            .replace(".", "_")
        val moduleType = TypeSpec.classBuilder("${clazzName}EljectModule")
            .addAnnotation(Module::class.java)
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("contribute${clazzName}")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .addAnnotation(contributeClazz)
                    .returns(ClassName.get(element))
                    .build()
            )

            .build()

        JavaFile.builder(packages.qualifiedName.toString(), moduleType)
            .build()
            .writeTo(filer)
    }

    private fun warn(message: String) {
        messager.printMessage(Diagnostic.Kind.WARNING, message)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Elject::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }
}
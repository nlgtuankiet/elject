package com.nlgtuankiet.elject.app

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nlgtuankiet.elject.Elject
import com.nlgtuankiet.elject.EljectModule
import com.nlgtuankiet.elject.Eljection
import com.nlgtuankiet.elject.HasAnyInjector
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject
import javax.inject.Singleton

@Elject
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Eljection.inject(this)
        println("MainActivity database is $database")
        WorkManager.getInstance(this).enqueue(
            OneTimeWorkRequest.from(MyWorker::class.java)
        )
    }
}

@Elject
class MyView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    @Inject
    lateinit var database: Database

    init {
        Eljection.inject(this)
        println("MyView database is $database")
    }
}

@Elject
class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    @Inject
    lateinit var database: Database

    init {
        Eljection.injectWith(context, this)
    }

    override fun doWork(): Result {
        println("MyWorker database is $database")
        return Result.success()
    }

}

@Singleton
class Database @Inject constructor()

class App : Application(), HasAnyInjector {

    @Inject
    lateinit var anyInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.factory().create(this).inject(this)

    }

    override fun anyInjector(): AndroidInjector<Any> {
        return anyInjector
    }

}

@Module(includes = [AppEljectModuleEljectModule::class])
@EljectModule
interface AppEljectModule

@Component(
    modules = [
        AndroidInjectionModule::class,
        AppEljectModule::class
    ]
)
@Singleton
interface AppComponent : AndroidInjector<App> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: App): AppComponent
    }
}



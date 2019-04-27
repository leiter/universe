package com.together

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton

@Module(includes = [
    AndroidInjectionModule::class,
    FirebaseModule::class,
    BuildersModule::class
    ])
interface AppModule {
    @Binds
    fun provideContext(app: App) : Application

}


@Singleton
@Component(modules = [AppModule::class])
interface AppComponent : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}

@Module
abstract class BuildersModule {
    @ContributesAndroidInjector(modules = [ProvideActivitiesModule::class])
    abstract fun bindMainActivity(): MainActivity

}

@Module
object ProvideActivitiesModule {
    @JvmStatic
    @Provides
    fun mainActivity(mainActivity: MainActivity) = mainActivity
    //bindPresenter
}

@Module
object FirebaseModule {

    @JvmStatic
    @Provides
    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @JvmStatic
    @Provides
    fun firebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

}

package com.together.base

//
//@Module(includes = [
//    AndroidInjectionModule::class,
//    BuildersModule::class
//    ])
//interface AppModule {
//    @Binds
//    fun provideContext(app: App) : Application
//}
//
//
//@Singleton
//@Component(modules = [AppModule::class])
//interface AppComponent : AndroidInjector<App> {
//    @Component.Builder
//    abstract class Builder : AndroidInjector.Builder<App>()
//}
//
//@Module
//abstract class FragmentModuleTest{
//
//    @Provides
//    fun firebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
//
//
//}
//
//@Module
//abstract class BuildersModule {
//    @ContributesAndroidInjector(modules = [ProvideActivitiesModule::class])
//    abstract fun bindMainActivity(): MainActivity
//
//
//}
//
//@Module
//object ProvideActivitiesModule {
//    @JvmStatic
//    @Provides
//    fun mainActivity(mainActivity: MainActivity) = mainActivity
//
//    @JvmStatic
//    @Provides
//    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
//
//    @JvmStatic
//    @Provides
//    fun firebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
//
//}

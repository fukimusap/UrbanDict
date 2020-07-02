package nike.urbandict.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import nike.urbandict.model.DefinitionsProcessor
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class FactoriesModule {

    @Singleton
    @Provides
    fun provideDefinitionsFactory(): DefinitionsProcessor {
        return DefinitionsProcessor()
    }

}

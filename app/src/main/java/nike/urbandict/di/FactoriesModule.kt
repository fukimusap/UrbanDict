package nike.urbandict.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import nike.urbandict.model.DefinitionsProcessor

@Module
@InstallIn(ApplicationComponent::class)
class FactoriesModule {

    @Provides
    fun provideDefinitionsFactory(): DefinitionsProcessor {
        return DefinitionsProcessor()
    }

}

package nike.urbandict.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import nike.urbandict.api.UrbanDictApi
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier


@Module
@InstallIn(ApplicationComponent::class)
class NetworkModule {

    @Provides
    fun provideApi(@ApplicationContext context: Context, @BaseUrl url: String, @Headers headers: Map<String, String>): UrbanDictApi {
        val httpCacheDirectory = File(context.cacheDir, "http-cache")
        val cacheSize = 10L * 1024L * 1024L
        val cache = Cache(httpCacheDirectory, cacheSize)

        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor {
                        it.proceed(it.request().newBuilder()
                            .apply {
                                headers.forEach { entry ->
                                    addHeader(entry.key, entry.value)
                                }
                            }
                            .build())
                    }
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .addInterceptor {
                        var response: Response? = null
                        val request = it.request()

                        try {
                            response = it.proceed(request)
                            if (response.isSuccessful) {
                                return@addInterceptor response
                            }
                        } catch (ignored: Exception) {
                        }

                        if (response == null || !response.isSuccessful) {
                            val cacheControl = CacheControl.Builder()
                                .maxStale(1, TimeUnit.DAYS)
                                .onlyIfCached()
                                .build()
                            it.proceed(request.newBuilder().cacheControl(cacheControl).build())
                        } else {
                            response
                        }
                    }
                    .addNetworkInterceptor {
                        val response = it.proceed(it.request())
                        if (response.isSuccessful) {
                            val cacheControl = CacheControl.Builder()
                                .maxAge(1, TimeUnit.DAYS)
                                .build()

                            response.newBuilder()
                                .removeHeader("Pragma")
                                .removeHeader("Cache-Control")
                                .header("Cache-Control", cacheControl.toString())
                                .build()
                        } else {
                            response
                        }
                    }
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .build())
            .build()
            .create(UrbanDictApi::class.java)
    }

    @Provides
    @Headers
    fun provideHeaders(): Map<String, String> {
        return mapOf(
            "x-rapidapi-host" to "mashape-community-urban-dictionary.p.rapidapi.com",
            "x-rapidapi-key" to "f2ba86279fmsh184468eceeb0123p116c5djsn2d8cd17af2d8"
        )
    }

    @Provides
    @BaseUrl
    fun provideUrl(): String {
        return "https://mashape-community-urban-dictionary.p.rapidapi.com"
    }
}

@Qualifier
annotation class BaseUrl

@Qualifier
annotation class Headers

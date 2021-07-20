package com.mystikcoder.statussaver.di

import android.content.Context
import com.google.gson.Gson
import com.mystikcoder.statussaver.background.api.BackgroundApiService
import com.mystikcoder.statussaver.background.repository.BackgroundFetcher
import com.mystikcoder.statussaver.networking.ApiService
import com.mystikcoder.statussaver.repository.*
import com.mystikcoder.statussaver.repository.instagram.*
import com.mystikcoder.statussaver.repository.mxtakatak.CallMxTakaTakInterface
import com.mystikcoder.statussaver.repository.mxtakatak.CallMxTakaTakRepository
import com.mystikcoder.statussaver.repository.twitter.CallTwitterInterface
import com.mystikcoder.statussaver.repository.twitter.CallTwitterRepository
import com.mystikcoder.statussaver.utils.FileClicked
import com.mystikcoder.statussaver.utils.PrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun providesInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Singleton
    @Provides
    fun providesOkHttpClientBuilder(
        interceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(2, TimeUnit.MINUTES)
        .connectTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .addInterceptor {

            var response: Response? = null

            try {
                val request: Request = it.request()
                response = it.proceed(request)
                if (response.code == 200) {
                    try {

                        val jsonObject = JSONObject(response.body?.string()!!)
                        val data = jsonObject.toString()
//                            printMsg(data)
                        val contentType = response.body?.contentType()
                        val responseBody = data.toResponseBody(contentType)

                        return@addInterceptor response.newBuilder().body(responseBody).build()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
            response!!
        }
        .addInterceptor(interceptor)
        .build()

    @Singleton
    @Provides
    fun providesRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://www.instagram.com/")
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun providesApiService(
        retrofit: Retrofit
    ): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun backgroundApiService(
        retrofit: Retrofit
    ): BackgroundApiService = retrofit.create(BackgroundApiService::class.java)

    @Singleton
    @Provides
    fun providesPrefManager(
        @ApplicationContext context: Context
    ): PrefManager = PrefManager(context)

    @Singleton
    @Provides
    fun providesCallResultRepository(api: ApiService): CallResultInterface =
        CallResultResult(api)

    @Singleton
    @Provides
    fun providesStoriesRepository(api: ApiService): StoriesApiInterface =
        StoriesApiRepository(api)

    @Singleton
    @Provides
    fun providesFullDetailsRepository(api: ApiService): FullDetailsInfoApiInterface =
        FullDetailsInfoApiRespository(api)

    @Singleton
    @Provides
    fun providesTwitterRepository(api: ApiService): CallTwitterInterface =
        CallTwitterRepository(api)

    @Singleton
    @Provides
    fun providesMxTakaTakRepository(api: ApiService): CallMxTakaTakInterface =
        CallMxTakaTakRepository(api)

    @Singleton
    @Provides
    fun providesInstagramDownloaderRepository(
        api: BackgroundApiService
    ): BackgroundFetcher =
        BackgroundFetcher(api)

    @Singleton
    @Provides
    fun providesFileClicked(
        @ApplicationContext context: Context
    ): FileClicked = FileClicked(context)
}

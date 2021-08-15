package com.mystikcoder.statussaver.di

import android.content.Context
import com.google.gson.Gson
import com.mystikcoder.statussaver.domain.networking.ApiService
import com.mystikcoder.statussaver.domain.networking.FacebookApiService
import com.mystikcoder.statussaver.domain.repository.*
import com.mystikcoder.statussaver.domain.repository.chingari.abstraction.ChingariDownloadRepository
import com.mystikcoder.statussaver.domain.repository.chingari.implementation.ChingariDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.facebook.abstraction.FacebookRepository
import com.mystikcoder.statussaver.domain.repository.facebook.implementation.FacebookRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.instagram.*
import com.mystikcoder.statussaver.domain.repository.instagram.abstraction.InstagramRepository
import com.mystikcoder.statussaver.domain.repository.instagram.implementation.InstagramRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.josh.abstraction.JoshDownloadRepository
import com.mystikcoder.statussaver.domain.repository.josh.implementation.JoshDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.likee.abstraction.LikeeDownloadRepository
import com.mystikcoder.statussaver.domain.repository.likee.implementation.LikeeDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.mitron.abstraction.MitronDownloadRepository
import com.mystikcoder.statussaver.domain.repository.mitron.implementation.MitronDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.moj.abstraction.MojDownloadRepository
import com.mystikcoder.statussaver.domain.repository.moj.implementation.MojDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.mxtakatak.abstraction.MxTakaTakDownloadRepository
import com.mystikcoder.statussaver.domain.repository.mxtakatak.implementation.MxTakaTakDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.roposo.abstraction.RoposoDownloadRepository
import com.mystikcoder.statussaver.domain.repository.roposo.implementation.RoposeDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.sharechat.abstraction.ShareChatDownloadRepository
import com.mystikcoder.statussaver.domain.repository.sharechat.implementation.ShareChatDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.tiktok.abstraction.TiktokDownloadRepository
import com.mystikcoder.statussaver.domain.repository.tiktok.implementation.TiktokDownloadRepositoryImpl
import com.mystikcoder.statussaver.domain.repository.twitter.abstraction.TwitterDownloadRepository
import com.mystikcoder.statussaver.domain.repository.twitter.implementation.TwitterDownloadRepositoryImpl
import com.mystikcoder.statussaver.presentation.utils.FileClicked
import com.mystikcoder.statussaver.presentation.utils.Preferences
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
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(okHttpClient)
    }

    @Singleton
    @Provides
    fun providesApiService(
        retrofitBuilder: Retrofit.Builder
    ): ApiService {
        val retrofit = retrofitBuilder.baseUrl("https://www.instagram.com/").build()
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun providesFacebookApi(
        retrofitBuilder: Retrofit.Builder
    ): FacebookApiService {
        val retrofit = retrofitBuilder.baseUrl("https://www.facebook.com/api/graphql/").build()
        return retrofit.create(FacebookApiService::class.java)
    }

    @Singleton
    @Provides
    fun providesPrefManager(
        @ApplicationContext context: Context
    ): Preferences = Preferences(context)

    @Singleton
    @Provides
    fun providesCallResultRepository(api: ApiService): InstagramRepository =
        InstagramRepositoryImpl(api)

    @Singleton
    @Provides
    fun providesTwitterRepository(api: ApiService): TwitterDownloadRepository =
        TwitterDownloadRepositoryImpl(api)

    @Singleton
    @Provides
    fun providesFileClicked(
        @ApplicationContext context: Context
    ): FileClicked = FileClicked(context)

    @Singleton
    @Provides
    fun providesChinghariRepository(): ChingariDownloadRepository{
        return ChingariDownloadRepositoryImpl()
    }

    @Singleton
    @Provides
    fun providesFacebookRepository(api: FacebookApiService): FacebookRepository{
        return FacebookRepositoryImpl(api)
    }

    @Singleton
    @Provides
    fun providesMitronRepository(): MitronDownloadRepository{
        return MitronDownloadRepositoryImpl()
    }

    @Singleton
    @Provides
    fun providesTakaTakRepository(api: ApiService): MxTakaTakDownloadRepository{
        return MxTakaTakDownloadRepositoryImpl(api)
    }

    @Singleton
    @Provides
    fun providesRoposoRepository(): RoposoDownloadRepository{
        return RoposeDownloadRepositoryImpl()
    }

    @Singleton
    @Provides
    fun providesTiktokRepository(api: ApiService): TiktokDownloadRepository{
        return TiktokDownloadRepositoryImpl(api)
    }

    @Singleton
    @Provides
    fun providesLikeeRepository(): LikeeDownloadRepository{
        return LikeeDownloadRepositoryImpl()
    }

    @Singleton
    @Provides
    fun providesJoshRepository(): JoshDownloadRepository{
        return JoshDownloadRepositoryImpl()
    }

    @Singleton
    @Provides
    fun providesShareChatRepository(): ShareChatDownloadRepository{
        return ShareChatDownloadRepositoryImpl()
    }

    @Singleton
    @Provides
    fun providesMojRepository(api: ApiService): MojDownloadRepository{
        return MojDownloadRepositoryImpl(api)
    }
}

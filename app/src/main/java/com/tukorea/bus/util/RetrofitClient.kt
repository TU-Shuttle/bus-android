package com.tukorea.bus.util

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://127.0.0.1:8080"
    private var retrofit: Retrofit? = null

    fun getInstance(context: Context): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val token = prefs.getString("jwt_token", null)

                val builder = original.newBuilder()
                if (token != null) {
                    builder.header("Authorization", "Bearer $token")
                }

                val request = builder.build()
                chain.proceed(request)
            }
            .build()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!
    }
}

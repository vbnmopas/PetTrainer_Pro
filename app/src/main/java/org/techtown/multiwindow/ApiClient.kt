package org.techtown.multiwindow

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api.openai.com/"

    // ChatGPTApi 인스턴스를 제공하는 lazy 객체
    val chatGPTApi: ChatGPTApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatGPTApi::class.java)  // 인터페이스로부터 인스턴스 생성
    }


}

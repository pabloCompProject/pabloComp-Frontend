package com.pablo.pablo

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MainInterface {

    @FormUrlEncoded
    @POST("member/serialNum")
    fun selectSerialCountPost(
        @Field("serialNum") serialNum: String
    ): Call<String>

}
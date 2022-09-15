package com.pablo.pablo.passwd

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PassWordInterface {

    @FormUrlEncoded
    @POST("member/tempPw")
    fun selectPwCountPost(
        @Field("serialNum") serialNum: String,
        @Field("tempPw") tempPw: String
    ): Call<String>

}
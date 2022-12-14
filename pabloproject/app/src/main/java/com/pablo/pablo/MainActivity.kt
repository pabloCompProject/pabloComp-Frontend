package com.pablo.pablo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.pablo.pablo.databinding.ActivityMainBinding
import com.pablo.pablo.passwd.PassWordActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var serialNum = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // QR 코드 버튼 이벤트
        binding.button.setOnClickListener {

            val integrator = IntentIntegrator(this)
            integrator.setCameraId(1) // 0은 후면, 1은 전면
            integrator.setOrientationLocked(false); //세로
            integrator.setBeepEnabled(false)
            integrator.setPrompt("QR코드를 인증해주세요.")
            integrator.initiateScan()

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // QR 코드를 찍은 결과를 변수에 담는다.
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        Log.d("TTT", "QR 코드 체크")

        //결과가 있으면
        if (result != null) {
            // 컨텐츠가 없으면
            if (result.contents == null) {
                //토스트를 띄운다.
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
            // 컨텐츠가 있으면
            else {
                //토스트를 띄운다.
                Toast.makeText(this, "scanned: " + result.contents, Toast.LENGTH_LONG).show()
                //serialNum 할당
                serialNum = result.contents
                selectSerialCountPost(serialNum)
            }
            // 결과가 없으면
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun selectSerialCountPost(serialNum: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.36.26.8:8080/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(MainInterface::class.java)
        val call: Call<String> = service.selectSerialCountPost(serialNum)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    var result = response.body().toString()

                    if(result.equals("YES")) {  //serialNum 이 DB에 있을 때
                        val intent = Intent(this@MainActivity, PassWordActivity::class.java)
                        intent.putExtra("serialNum", serialNum)
                        startActivity(intent)
                    }
                } else {    //serialNum 이 DB에 없을 때
                    Log.d("Reg", "onResponse Failed")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("Reg", "error : " + t.message.toString())
            }
        })
    }
}
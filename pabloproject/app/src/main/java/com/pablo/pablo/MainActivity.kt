package com.pablo.pablo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.pablo.pablo.databinding.ActivityMainBinding
import com.pablo.pablo.passwd.PassWordActivity
import org.json.JSONObject
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

        binding.button2.setOnClickListener {
            val intent = Intent(this, PassWordActivity::class.java)
            startActivity(intent)
        }

        // QR 코드 버튼 이벤트
        binding.button.setOnClickListener {

            val integrator  = IntentIntegrator(this)
            integrator.setOrientationLocked(false); // default가 세로모드인데 휴대폰 방향에 따라 가로, 세로로 자동 변경됩니다.
            integrator.setBeepEnabled(false)
            integrator.setOrientationLocked(true)
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
                // Log.d("TTT", "QR 코드 URL:${result.contents}")

                //웹뷰 설정 - 필요 X
                // binding.webView.settings.javaScriptEnabled = true
                // binding.webView.webViewClient = WebViewClient()

                //웹뷰를 띄운다. - 필요 X
                // binding.webView.loadUrl(result.contents)
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
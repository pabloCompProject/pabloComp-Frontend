package com.pablo.pablo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.pablo.pablo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}
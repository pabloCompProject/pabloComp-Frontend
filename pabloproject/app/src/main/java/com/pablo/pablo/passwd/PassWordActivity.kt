package com.pablo.pablo.passwd

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pablo.pablo.R
import com.pablo.pablo.databinding.PasswdMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private lateinit var binding: PasswdMainBinding

class PassWordActivity : AppCompatActivity(){

    var serialNum = ""
    var tempPw = ""
    //override fun 에서 this 컨텍스트 사용하기 위한 코드
    init {
        instance = this
    }
    companion object {
        lateinit var instance: PassWordActivity
        fun PassWordActivityContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PasswdMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val buttonArray = arrayListOf<Button>(binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4, binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9, binding.btnDel)
        for (button in buttonArray){
            button.setOnClickListener(btnListener)
        }

        //MainActivity 에서 serialNum 받아오기
        if(intent.hasExtra("serialNum")) {
            serialNum = intent.getStringExtra("serialNum").toString()
        }

        //확인 버튼 이벤트
        binding.btnInsert.setOnClickListener {
            tempPw = inputedPasswd()
            selectPwCountPost(serialNum, tempPw)
            Log.d("code -------", tempPw)
        }
    }

    //비번 합치기
    private fun inputedPasswd():String {
        return "${binding.passcode1.text}${binding.passcode2.text}${binding.passcode3.text}${binding.passcode4.text}"}
    }

    //edit text
    private fun setEditText(currentEditText: EditText, nextEditText: EditText, strCurrentValue : String) {
        currentEditText.setText(strCurrentValue)
        nextEditText.requestFocus()
        nextEditText.setText("")
    }

    //한 칸 지우기 -> 마지막 부분 제대로 안지워짐.
    private fun onDeleteKey() {
        when {
            binding.passcode1.isFocused -> {
                binding.passcode1.setText("")
            }
            binding.passcode2.isFocused -> {
                binding.passcode2.setText("")
                binding.passcode1.requestFocus()
            }
            binding.passcode3.isFocused -> {
                binding.passcode3.setText("")
                binding.passcode2.requestFocus()
            }
            binding.passcode4.isFocused -> {
                binding.passcode4.setText("")
                binding.passcode3.requestFocus()
            }
        }
    }

    //전체 삭제
    private fun onClear() {
        binding.passcode1.setText("")
        binding.passcode2.setText("")
        binding.passcode3.setText("")
        binding.passcode4.setText("")
        binding.passcode1.requestFocus()
    }

    //retrofit 통신
    private fun selectPwCountPost(serialNum: String, tempPw: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.36.26.8:8080/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(PassWordInterface::class.java)
        val call: Call<String> = service.selectPwCountPost(serialNum, tempPw)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    var result = response.body().toString()

                    if(result.equals("YES")) {  //임시 비밀번호가 맞을 때
                        Toast.makeText(PassWordActivity.PassWordActivityContext(), "인증 성공", Toast.LENGTH_LONG).show()
                    }
                } else {    //임시 비밀번호가 틀렸을 때
                    Toast.makeText(PassWordActivity.PassWordActivityContext(), "인증 실패", Toast.LENGTH_LONG).show()
                    Log.d("Reg", "onResponse Failed")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("Reg", "error : " + t.message.toString())
            }
        })
    }

    //버튼 클릭
    private val btnListener = View.OnClickListener { view ->
        var currentValue = -1
        when(view.id){
            R.id.btn0 -> currentValue = 0
            R.id.btn1 -> currentValue = 1
            R.id.btn2 -> currentValue = 2
            R.id.btn3 -> currentValue = 3
            R.id.btn4 -> currentValue = 4
            R.id.btn5 -> currentValue = 5
            R.id.btn6 -> currentValue = 6
            R.id.btn7 -> currentValue = 7
            R.id.btn8 -> currentValue = 8
            R.id.btn9 -> currentValue = 9
            R.id.btnDel -> onDeleteKey()
    }

    val strCurrentValue = currentValue.toString() //비번 전체 String으로
    if (currentValue != -1) {
        when {
            binding.passcode1.isFocused -> {
                setEditText(binding.passcode1, binding.passcode2, strCurrentValue)
            }
            binding.passcode2.isFocused -> {
                setEditText(binding.passcode2, binding.passcode3, strCurrentValue)
            }
            binding.passcode3.isFocused -> {
                setEditText(binding.passcode3, binding.passcode4, strCurrentValue)
            }
            binding.passcode4.isFocused -> {
                binding.passcode4.setText(strCurrentValue)
            }
        }
    }
}
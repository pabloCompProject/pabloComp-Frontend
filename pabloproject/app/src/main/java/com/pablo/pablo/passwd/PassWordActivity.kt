package com.pablo.pablo.passwd

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.pablo.pablo.R
import com.pablo.pablo.databinding.PasswdMainBinding

private lateinit var binding: PasswdMainBinding

class PassWordActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PasswdMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val buttonArray = arrayListOf<Button>(binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4, binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9, binding.btnClear, binding.btnDel)
        for (button in buttonArray){
            button.setOnClickListener(btnListener)
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
            R.id.btnClear -> onClear()
            R.id.btnDel -> onDeleteKey()
    }

        fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
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

    /*// 비밀번호 모두 입력시
    if (Passcode1.text.isNotEmpty() && etPasscode2.text.isNotEmpty() && etPasscode3.text.isNotEmpty() && etPasscode4.text.isNotEmpty()) {
        inputType(intent.getIntExtra("type", 0))
    }*/
}
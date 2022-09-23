package com.pablo.pablo.passwd

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pablo.pablo.R
import com.pablo.pablo.bluetooth.ConnectedThread
import com.pablo.pablo.databinding.PasswdMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.lang.reflect.Method
import java.util.*

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
        private const val REQUEST_ENABLE_BT = 1
    }

    ////////////////////////////////////////////
    var btAdapter: BluetoothAdapter? = null
    var pairedDevices: Set<BluetoothDevice>? = null
    var deviceAddressArray: ArrayList<String>? = null
    var connectedThread : ConnectedThread? = null
    private val BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device!!.name
                val deviceHardwareAddress = device.address // MAC address
                deviceAddressArray!!.add(deviceHardwareAddress)
            }
        }
    }
    ////////////////////////////////////////////

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PasswdMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ////////////////////////////////////////////
        setPermission() //권한체크

        deviceAddressArray = ArrayList()

        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!btAdapter!!.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent!!, PassWordActivity.REQUEST_ENABLE_BT)
        }

        if (deviceAddressArray != null && !deviceAddressArray!!.isEmpty()) {
            deviceAddressArray!!.clear()
        }
        pairedDevices = btAdapter!!.bondedDevices
        if (pairedDevices!!.size > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (device in pairedDevices!!) {
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                deviceAddressArray!!.add(deviceHardwareAddress)
            }
        }

        val name: String = "PabloB" // get name
        val address: String = "00:21:06:08:32:82" // get address
        var flag = true
        val device: BluetoothDevice = btAdapter!!.getRemoteDevice(address)
        var btSocket : BluetoothSocket? = null

        // create & connect socket
        try {
            btSocket = createBluetoothSocket(device)!!
            btSocket!!.connect()
        } catch (e: IOException) {
            flag = false
            e.printStackTrace()
        }

        if (flag) {
            connectedThread = ConnectedThread(btSocket!!)
            connectedThread!!.start()
            Log.d("Test!", "Success!")
        }
        ////////////////////////////////////////////

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
            selectPwCountPost(serialNum, tempPw, connectedThread!!)
            Log.d("code -------", tempPw)
        }
    }

    ///////////////////////////////////////
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1000) {
            var check_result : Boolean = true;

            for(result in grantResults) {
                if(result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if(check_result == true) {

            } else {
                finish()
            }
        }
    }

    private fun setPermission() {
        var scanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        var connectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        var advertisePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)
        var finePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        var coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if(scanPermission == PackageManager.PERMISSION_DENIED ||
            connectPermission == PackageManager.PERMISSION_DENIED ||
            advertisePermission == PackageManager.PERMISSION_DENIED ||
            finePermission == PackageManager.PERMISSION_DENIED ||
            coarsePermission == PackageManager.PERMISSION_DENIED) {

            var permissions = arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, permissions, 1000)
            }

            return;
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        try {
            val m: Method = device.javaClass.getMethod(
                "createInsecureRfcommSocketToServiceRecord",
                UUID::class.java
            )
            return m.invoke(device, BT_MODULE_UUID) as BluetoothSocket?
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Could not create Insecure RFComm Connection", e)
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID)
    }
    ///////////////////////////////////////

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
private fun selectPwCountPost(serialNum: String, tempPw: String, connectedThread: ConnectedThread) {
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
                    if(connectedThread != null) {
                        connectedThread!!.write("YES");
                    }
                } else {
                    Toast.makeText(PassWordActivity.PassWordActivityContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show()
                    if(connectedThread != null) {
                        connectedThread!!.write("NO");
                    }
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
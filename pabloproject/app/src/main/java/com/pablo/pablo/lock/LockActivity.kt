package com.pablo.pablo.lock

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.system.Os.socket
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pablo.pablo.bluetooth.ConnectedThread
import com.pablo.pablo.databinding.LockMainBinding
import java.io.IOException
import java.lang.reflect.Method
import java.util.*


class LockActivity : AppCompatActivity() {
    private lateinit var binding: LockMainBinding

    companion object {
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
        binding = LockMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ////////////////////////////////////////////
        setPermission() //권한체크

        deviceAddressArray = ArrayList()

        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!btAdapter!!.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent!!, LockActivity.REQUEST_ENABLE_BT)
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
            try {
                btSocket = device.javaClass.getMethod(
                    "createRfcommSocket", *arrayOf<Class<*>?>(
                        Int::class.javaPrimitiveType
                    )
                ).invoke(device, 2) as BluetoothSocket
                btSocket!!.connect()

                flag = true
            } catch(e2: Exception) {
                flag = false
                e.printStackTrace()
            }
        }

        if (flag) {
            connectedThread = ConnectedThread(btSocket!!)
            connectedThread!!.start()
            Log.d("Test!", "Lock Success")
        }
        ////////////////////////////////////////////

        // 잠금 코드 버튼 이벤트
        binding.lockBtn.setOnClickListener {
            if(connectedThread != null) {
                connectedThread!!.write("NO");
            }

            Toast.makeText(this, "잠금 완료", Toast.LENGTH_LONG).show()
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
}
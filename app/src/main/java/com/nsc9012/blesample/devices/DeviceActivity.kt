package com.nsc9012.blesample.devices

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.nsc9012.blesample.R
import com.nsc9012.blesample.extensions.invisible
import com.nsc9012.blesample.extensions.toast
import com.nsc9012.blesample.extensions.visible
import kotlinx.android.synthetic.main.activity_devices.*

class DeviceActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner

    private val deviceListAdapter = DevicesAdapter()

    /* Listen for scan results */
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            deviceListAdapter.addDevice(result.device)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        initBLE()
        initUI()
    }

    private fun initUI() {

        title = getString(R.string.ble_scanner)

        recycler_view_devices.adapter = deviceListAdapter
        recycler_view_devices.layoutManager = LinearLayoutManager(this)
        recycler_view_devices.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        button_discover.setOnClickListener {
            with(button_discover) {
                if (text == getString(R.string.start_scanning)) {
                    deviceListAdapter.clearDevices()
                    text = getString(R.string.stop_scanning)
                    progress_bar.visible()
                    startScanning()
                } else {
                    text = getString(R.string.start_scanning)
                    progress_bar.invisible()
                    stopScanning()
                }
            }
        }

    }

    private fun initBLE() {

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_COARSE_LOCATION
            )
        }

    }

    private fun startScanning() {
        AsyncTask.execute { bluetoothLeScanner.startScan(leScanCallback) }
    }

    private fun stopScanning() {
        AsyncTask.execute { bluetoothLeScanner.stopScan(leScanCallback) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted
                } else {
                    toast("Without location access, this app cannot discover beacons.")
                }
            }
        }
    }
}

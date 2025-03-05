package com.example.hellofigma

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WarehouseDetailsActivity : AppCompatActivity() {

    private lateinit var btnConfirmLocation: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warehouse_details)

        val warehouseName = intent.getStringExtra("WAREHOUSE_NAME") ?: "Unknown Warehouse"
        val warehouseAddress = intent.getStringExtra("WAREHOUSE_ADDRESS") ?: "Address not available"

        val tvWarehouseName = findViewById<TextView>(R.id.tvWarehouseName)
        val tvWarehouseAddress = findViewById<TextView>(R.id.tvWarehouseAddress)

        tvWarehouseName.text = warehouseName
        tvWarehouseAddress.text = warehouseAddress

        btnConfirmLocation = findViewById(R.id.btnConfirmLocation)
        btnConfirmLocation.setOnClickListener {
            finish()
        }


    }
}

package com.okaythis.jasvir.ui

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.okaythis.jasvir.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val vm: MainViewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClick()
        setObservers()
    }

    private fun setClick() {
        binding.etTime.setOnClickListener {
            showTimeDialog()
        }
    }


    private fun setObservers() {
        vm.transactions.observe(this) {
            setUpRecyclerView(it)
        }
    }


    private fun setUpRecyclerView(list: ArrayList<String>) {
        binding.rvTransaction.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = PaymentAdapter(list)
        }
    }


    private fun showTimeDialog() {
        val mCurrentTime: Calendar = Calendar.getInstance()
        val hour: Int = mCurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute: Int = mCurrentTime.get(Calendar.MINUTE)
        val mTimePicker = TimePickerDialog(
            binding.etTime.context,
            { timePicker, selectedHour, selectedMinute -> binding.etTime.setText("$selectedHour:$selectedMinute") },
            hour,
            minute,
            true
        ) //Yes 24 hour time

        mTimePicker.setTitle("Select Time")
        mTimePicker.show()
    }

}
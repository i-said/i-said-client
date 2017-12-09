package spaceapps.isaid.jp.pilotplus

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSpinner
import android.view.View
import android.widget.ArrayAdapter

import spaceapps.isaid.jp.pilotplus.databinding.ActivityFirstBinding

class FirstActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirstBinding
    private lateinit var spinner: AppCompatSpinner
    private lateinit var btnNext: View
    private lateinit var adapter: ArrayAdapter<CharSequence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_first)
        btnNext = binding.btnNext
        spinner = binding.spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this, R.array.airplane_array, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        spinner.adapter = adapter

        btnNext.setOnClickListener {
            startNextActivity(adapter.getItem(spinner.selectedItemPosition) as String)
        }
    }

    private fun startNextActivity(airplane: String) {
        val intent = Intent(this@FirstActivity, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_AIRPLANE, airplane)
        startActivity(intent)

    }
}

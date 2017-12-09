package spaceapps.isaid.jp.pilotplus

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSpinner
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import spaceapps.isaid.jp.pilotplus.databinding.ActivityFirstBinding

class FirstActivity : AppCompatActivity() {

    private var mSpinner: AppCompatSpinner? = null
    private var mBtnNext: View? = null
    private var mAdapter: ArrayAdapter<CharSequence>? = null
    private var mBinding: ActivityFirstBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_first)
        mBtnNext = mBinding!!.btnNext
        mSpinner = mBinding!!.spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        mAdapter = ArrayAdapter.createFromResource(this,
                R.array.airplane_array, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        mAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        mSpinner!!.adapter = mAdapter

        mBtnNext!!.setOnClickListener { startNextActivity(mAdapter!!.getItem(mSpinner!!.selectedItemPosition) as String) }


    }


    override fun onResume() {
        super.onResume()
        mSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {}

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

    }

    private fun startNextActivity(airplane: String) {
        val intent = Intent(this@FirstActivity, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_AIRPLANE, airplane)
        startActivity(intent)

    }
}

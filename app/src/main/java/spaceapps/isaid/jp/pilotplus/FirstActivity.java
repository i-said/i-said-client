package spaceapps.isaid.jp.pilotplus;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import spaceapps.isaid.jp.pilotplus.databinding.ActivityFirstBinding;

public class FirstActivity extends AppCompatActivity {

    private AppCompatSpinner mSpinner;
    private View mBtnNext;
    private ArrayAdapter<CharSequence> mAdapter;
    private ActivityFirstBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_first);
        mBtnNext = mBinding.btnNext;
        mSpinner = mBinding.spinner;
// Create an ArrayAdapter using the string array and a default spinner layout
        mAdapter = ArrayAdapter.createFromResource(this,
                R.array.airplane_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinner.setAdapter(mAdapter);

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNextActivity((String) mAdapter.getItem(mSpinner.getSelectedItemPosition()));
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void startNextActivity(String airplane) {
        Intent intent = new Intent(FirstActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_AIRPLANE, airplane);
        startActivity(intent);

    }
}

package cc.leniwie.burned_calories;

import android.os.Handler;
import android.os.Bundle;
import java.util.Arrays;

import io.hammerhead.sdk.v0.KeyValueStore;
import io.hammerhead.sdk.v0.SdkContext;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View;
import android.text.TextWatcher;
import android.text.Editable;

import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

  private static final String[] genders = {"Male", "Female"};

  private EditText editAge, editWeight;
  private EditText updateDrift;
  private Spinner spinner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_activity);

    KeyValueStore kvStore = SdkContext.buildSdkContext(this).getKeyValueStore();

    editAge = (EditText) findViewById(R.id.editAge);
    editWeight = (EditText) findViewById(R.id.editWeight);
    updateDrift = (EditText) findViewById(R.id.updateDrift);
    updateDrift.setText(kvStore.getDouble("updateDrift").toString());
    
    spinner = (Spinner) findViewById(R.id.spinner);
    ArrayAdapter<String>adapter = new ArrayAdapter<String>(MainActivity.this,
      android.R.layout.simple_spinner_item, genders);

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);

    spinner.post(new Runnable() {
      @Override
      public void run() {
        spinner.setSelection(Arrays.asList(genders).indexOf(kvStore.getString(getString(R.string.genderKey))));
      }
    });

    long age = Math.round(kvStore.getDouble(getString(R.string.ageKey)));
    editAge.setText(String.valueOf(age));
    editAge.addTextChangedListener(new TextWatcher() {

      @Override
      public void afterTextChanged(Editable s) {}

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence sequence, int start, int before, int count) {
          if(sequence.length() != 0) {
            kvStore.putString(getString(R.string.ageKey), sequence.toString());
          }
      }
    });


    editWeight.setText(kvStore.getDouble(getString(R.string.weightKey)).toString());
    editWeight.addTextChangedListener(new TextWatcher() {

      @Override
      public void afterTextChanged(Editable s) {}

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence sequence, int start, int before, int count) {
          if(sequence.length() != 0) {
            kvStore.putString(getString(R.string.weightKey), sequence.toString());
          }
      }
    });
  
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
    Timber.d("Selected gender: " + genders[position]);
    KeyValueStore kvStore = SdkContext.buildSdkContext(this).getKeyValueStore();
    kvStore.putString(getString(R.string.genderKey), genders[position]);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {}

}
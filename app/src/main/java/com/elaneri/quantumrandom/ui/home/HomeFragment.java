package com.elaneri.quantumrandom.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.elaneri.quantumrandom.R;
import com.elaneri.quantumrandom.util.MinMaxFilter;
import com.elaneri.quantumrandom.worker.RandomNumbers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private static final String KEY_RESULT = "KEY_RESULT";
    private static final String MAX_RND = "MAX_RND";
    private static final String CNT_RND = "CNT_RND";
    private static final String TYPE_RND = "TYPE_RND";

    private static final String[] pickerTypes = new String[] {"uint8", "uint16"};

    //private static final String[] pickerTypes = new String[] {"uint8", "uint16", "hex16"};
    private ProgressBar progressDialog;
    private EditText txtmaxRnd;
    private NumberPicker picTypes;
    private  String sep;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView randomstrView = root.findViewById(R.id.txt_randomstr);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
              //  randomstrView.setText(s);
            }
        });

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        String defaultSep = getResources().getString(R.string.numdefsep);
        sep = sharedPref.getString(getString(R.string.numsep), defaultSep);


        String defaultRandom = getResources().getString(R.string.defRrandomnums);
        String randomNums = sharedPref.getString(getString(R.string.randomnums), defaultRandom);
        EditText   txtRnd = root.findViewById(R.id.txt_randomstr) ;

        txtRnd.setText(randomNums);

        txtmaxRnd = root.findViewById(R.id.maxRnd) ;
        txtmaxRnd.setFilters( new InputFilter[]{ new MinMaxFilter( "1" , "65535" )});

        picTypes = root.findViewById(R.id.rndType);
        picTypes.setMaxValue(1);
        picTypes.setMinValue(0);
        picTypes.setDisplayedValues(pickerTypes);

        progressDialog= root.findViewById(R.id.progressBar) ;

        progressDialog.setMax(100);
        progressDialog.setProgress(0);


        final Button button = root.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setProgress(0);
                int valuePicker1 = picTypes.getValue();
                Data data = new Data.Builder()
                        .putString(CNT_RND,"1")
                        .putString(MAX_RND, txtmaxRnd.getText().toString())
                        .putString(TYPE_RND,  pickerTypes[valuePicker1] )
                        .build();

                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(RandomNumbers.class).setInputData(data).build();
                WorkManager.getInstance(v.getContext()).enqueue(workRequest);
                progressDialog.setProgress(25);

                // Get the work status
                WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(workRequest.getId())
                        .observe(getViewLifecycleOwner(), new Observer<WorkInfo>() {
                            @Override
                            public void onChanged(WorkInfo workInfo) {

                                progressDialog.setProgress(progressDialog.getProgress()+10);

                                if (workInfo.getState().equals(WorkInfo.State.SUCCEEDED)){
                                    try {
                                        JSONObject qRandom=new JSONObject( workInfo.getOutputData().getString(KEY_RESULT));
                                        JSONArray jsonRandNum = qRandom.getJSONArray("data");

                                        randomstrView.setText(jsonRandNum.toString()
                                                .substring(1,jsonRandNum.toString().length()-1).replaceAll(",",sep));



                                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();

                                        editor.putString(getString(R.string.randomnums), randomstrView.getText().toString());

                                        editor.apply();



                                        progressDialog.setProgress(100);

                                    } catch (JSONException e) {

                                        Log.d("", "Worker error: " +   e.toString());
                                    }
                                }
                                Log.d("", "Worker status: " + workInfo.getState().name());
                            }
                        });


            }
        });


        return root;
    }
}
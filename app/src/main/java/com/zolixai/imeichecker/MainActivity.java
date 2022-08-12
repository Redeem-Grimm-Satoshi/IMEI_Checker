package com.zolixai.imeichecker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;


/*

Author: Redeem_Grimm
Description: Scraps IMEI Codes From Orange Servers Checking If They're Valid For The Orange Free Net Activation Or Not.

 */

public class MainActivity extends AppCompatActivity {


    //Global V
    EditText getCode;
    MaterialButton checkCode;
    TextView displayCode,networkName;

    //Minors
    int i = 0;
    int sim;

    //message which checks if it's valid or not
    String validFrench="Veuillez rentrer le numero de telephone du beneficiaire et Validez";
    String validEnglish="Please enter the beneficiary's phone number and Validate";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();


        //TelephonyManager Instantiated
        TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);


        //Global V ID
        getCode = findViewById(R.id.getCode);
        checkCode = findViewById(R.id.checkCode);
        displayCode = findViewById(R.id.displayCode);
        networkName=findViewById(R.id.sim_name);



        //get sim name
        String getSimName=manager.getNetworkOperatorName();

        //Set the name to textview
        networkName.setText("Hello! You're Using: " + getSimName + " | " + Build.MANUFACTURER);

        //Dialog Box for Instructions
        MaterialAlertDialogBuilder dialogBuilder=new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("IMEI Checker 1.5 Info");
        dialogBuilder.setMessage("Instructions\n\n 1) error code: -1 means request didn't reach server. \n\n 2) Valid IMEI Codes are displayed in the box below \n\n 3) Multi-session wrapper coming soon!  \n\n\nDeveloper: Redeem_Grimm");
        dialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                messenger("Welcome " + Build.MANUFACTURER);
            }
        });
        AlertDialog alertDialog=dialogBuilder.create();
        alertDialog.show();






        //When Button Clicked It Starts Checking
        checkCode.setOnClickListener(view -> {
            //check is IMEI code is empty or not
            if(TextUtils.isEmpty(getCode.getText().toString())){
                messenger("Please Enter IMEI Codes!");
            }else {

                //if it's not empty, it executes this function
                scrapIMEI();
            }
        });




    }


    //start the process
    void scrapIMEI(){
        //String code = getCode.getText().toString();
        String[] IMEI = getCode.getText().toString().split("\n");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 234);
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
                TelephonyManager manager2 = manager.createForSubscriptionId(2);



                //automatically selects the first sim ( not dual sim capable for now )
                TelephonyManager managerMain = (sim == 0) ? manager : manager2;
                StringBuilder builder = new StringBuilder();


                //this part is tricky, multi-session ussd request update coming soon!
                for (i = 0; i < IMEI.length; i++) {
                    if (IMEI[i].equalsIgnoreCase("")) return;


                    managerMain.sendUssdRequest("#195*1*" + IMEI[i] + "#", new TelephonyManager.UssdResponseCallback() {
                        @Override
                        public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                            super.onReceiveUssdResponse(telephonyManager, request, response);
                            String result = response.toString();
                            if (result.contains(validFrench)|| result.contains(validEnglish)) {
                                builder.append(IMEI[i] + "\n");
                                getCode.setText(IMEI[i] + "");

                                messenger("IMEI Validated");
                            } else {
                                messenger("IMEI Invalid");

                            }


                            Log.e("TAG", "onReceiveUssdResponse:  Ussd Response = " + response.toString().trim());
                           messenger(response.toString());


                        }


                        @Override
                        public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                            super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);

                            getCode.setText(" ");

                            Log.e("TAG", "onReceiveUssdResponseFailed: " + " " + failureCode + request);

                            try {
                                Toast.makeText(MainActivity.this, failureCode + " ", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                System.out.println("Ooops! Request Didn't Reach Server");
                            }

                        }
                    }, new Handler());


                    //adds/appends valid IMEI to textview below
                    displayCode.setText(builder.toString());
                }
            }
        }

    }

    //messenger
    void messenger(String message){
        Toast.makeText(MainActivity.this, message,Toast.LENGTH_SHORT).show();
    }
}














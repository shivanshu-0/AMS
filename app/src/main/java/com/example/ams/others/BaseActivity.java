package com.example.ams.others;

import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;

/*This is the Activity extended by every other activity. It provides basic progress Dialog to display.
* Since every Activity involves connection to server which cannot be done on UI Thread, therefore to
* keep every activity interactive we need to display progress dialog.*/

public class BaseActivity extends AppCompatActivity {

    public ProgressDialog progressDialog;

    public void showProgressDialog( String message){

        if(progressDialog==null){

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);

        }
        progressDialog.show();
    }

    public void hideProgressDialog(){

        if(progressDialog!=null && progressDialog.isShowing())
            progressDialog.dismiss();

    }
    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}

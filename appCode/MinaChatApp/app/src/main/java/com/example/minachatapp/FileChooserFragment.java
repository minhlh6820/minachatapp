package com.example.minachatapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

// References: https://openplanning.net/12725/tao-mot-file-chooser-don-gian-trong-android
public class FileChooserFragment extends Fragment {
    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;

    private Button browseBtn;
    private EditText pathEditTxt;
    private File file;

    public FileChooserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_file_chooser, container, false);

        this.pathEditTxt = (EditText) rootView.findViewById(R.id.pathEditTxt);
        this.browseBtn = (Button) rootView.findViewById(R.id.browseBtn);

        browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermissionAndBrowseFile();
            }
        });
        return rootView;
    }

    private void askPermissionAndBrowseFile() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        //Ask permission to access External Storage with Android Level >= 23

        if(Build.VERSION.SDK_INT >= 23) {
            //Check Call permission
            for(String str: PERMISSIONS_STORAGE) {
                int permissions =  ActivityCompat.checkSelfPermission(this.getContext(), str);
                if(permissions != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(PERMISSIONS_STORAGE, MY_REQUEST_CODE_PERMISSION);
                    return;
                }
            }
        }
//        System.out.println(readPermission + "-" + writePermission);
        this.doBrowseFile();
    }

    private void doBrowseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        //Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent = Intent.createChooser(intent, "Choose a file");
        startActivityForResult(intent, MY_RESULT_CODE_FILECHOOSER);
    }

    //When have request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case MY_REQUEST_CODE_PERMISSION:
//                System.out.println("Request code: " + requestCode);
                //If request is cancelled, result arrays are empty
                //Permissions granted (CALL_PHONE)
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this.getContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
//                    System.out.println("Permission granted!");
                    this.doBrowseFile();
                } else {
                    //Permissions denied/cancelled
                    Toast.makeText(this.getContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
//                    System.out.println("Permission granted!");
                }
                break;
            default:
                System.out.println("Request code: " + requestCode);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case MY_RESULT_CODE_FILECHOOSER:
                if(resultCode == Activity.RESULT_OK) {
                    if(data != null) {
                        Uri fileUri = data.getData();
                        String path = null;
                        try {
                            path = FileUtils.getPath(this.getContext(), fileUri);
                            file = new File(path);
                            path = file.toString();
                        } catch (Exception e) {
                            Toast.makeText(this.getContext(), "Error!" + e, Toast.LENGTH_SHORT).show();
                        }
                        this.pathEditTxt.setText(path);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public File getFile() {
        return this.file;
    }
}
package com.bourne.caesar.impextutors.UI_Components.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bourne.caesar.impextutors.R;
import com.bourne.caesar.impextutors.FirebaseTasksCore.ChangeUserPassword;
import com.bourne.caesar.impextutors.UI_Components.Activities.MyPurchasesActivity;
import com.bourne.caesar.impextutors.Utilities.Constants;
import com.bourne.caesar.impextutors.Utilities.SharedPreferencesStorage;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountFragment extends Fragment {

    private ChangeUserPassword changeUserPassword;
    private Button changePasswordView, editProfileView, paidCoursesView, setCurrencyButton;
    CircleImageView userProfileView;
    private AlertDialog.Builder changePasswordDialog;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 1;
    private FirebaseAuth authenticatedUser;
    private TextView usernameView;
    private TextView userEmailView;
    private RadioGroup radioGroup;
    private RadioButton nairaButton,dollarButton;

    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeUserPassword = new ChangeUserPassword(this);
        authenticatedUser = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);
        initialization(view);
        viewsAction();
        if (SharedPreferencesStorage.getSharedPrefInstance(getActivity()).getUserImages(Constants.IMPEX_USER_IMAGE)!= null) {

            String userPic = SharedPreferencesStorage.getSharedPrefInstance(getActivity()).getUserImages(Constants.IMPEX_USER_IMAGE);
            Picasso.get().load(userPic).into(userProfileView);
        }
        return view;
    }



    private void initialization(View view) {
        changePasswordView = view.findViewById(R.id.changePasswordView);
        changePasswordDialog = new AlertDialog.Builder(getActivity());
        userProfileView = view.findViewById(R.id.userProfileView);
        editProfileView = view.findViewById(R.id.editprofileView);
        usernameView = view.findViewById(R.id.usernameView);
        userEmailView = view.findViewById(R.id.userEmailView);
        radioGroup = view.findViewById(R.id.radioGroup);
        nairaButton = view.findViewById(R.id.nairaPay);
        dollarButton = view.findViewById(R.id.dollarPay);
        paidCoursesView = view.findViewById(R.id.myPaidCoursesView);
        setCurrencyButton = view.findViewById(R.id.setCurrencyView);
    }

//    public void onRadioButtonClicked(View view){
//        int id  = radioGroup.getCheckedRadioButtonId();
//        switch (id){
//            case R.id.nairaPay:
//                SharedPreferencesStorage.getSharedPrefInstance(getActivity()).saveCurrency( Constants.IMPEX_NAIRA);
//                break;
//            case R.id.dollarPay:
//                SharedPreferencesStorage.getSharedPrefInstance(getActivity()).saveCurrency( Constants.IMPEX_DOLLAR);
//                break;
//        }
//    }
    public static boolean hasUniqueChars( String str ) {
        boolean [] booleancharacters= new boolean[256];
        for(int i=0; i< str.length(); i++){

            int characterstate = str.charAt(i);
            if(booleancharacters[characterstate]){
                return false;
            }
            booleancharacters[characterstate] = true;
        }
        return true;
    }


static int shiftedDiff(String first, String second){
    int n=0;
    if (first.length() == second.length() && (first+first).contains(second) ){
        while (!TextUtils.equals(first, second)){
            first = first.charAt(first.length() - 1) + first.substring(0, first.length() -1);
            n = +1;
        }
    }

    return n;
}
    private void viewsAction() {
        String username = authenticatedUser.getCurrentUser().getDisplayName();
        String userEmail = authenticatedUser.getCurrentUser().getEmail();

        usernameView.setText(username);
        userEmailView.setText(userEmail);
        changePasswordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChangePasswordDialog();

            }
        });

        editProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);


            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.nairaPay:
                        SharedPreferencesStorage.getSharedPrefInstance(getActivity()).saveCurrency( Constants.IMPEX_NAIRA);
                        break;
                    case R.id.dollarPay:
                        SharedPreferencesStorage.getSharedPrefInstance(getActivity()).saveCurrency( Constants.IMPEX_DOLLAR);
                        break;
                }
            }
        });
        if ((SharedPreferencesStorage.getSharedPrefInstance(getActivity()).getCurrency() == Constants.IMPEX_DOLLAR)){
            dollarButton.setChecked(true);

        }
        else {
            nairaButton.setChecked(true);
        }

        paidCoursesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyPurchasesActivity.class);
                startActivity(intent);
            }
        });

        setCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getVisibility() == View.VISIBLE ){
                    radioGroup.setVisibility(View.GONE);
                }else {
                    radioGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void startChangePasswordDialog() {
        changePasswordDialog.setIcon(R.drawable.impexlogo);
        changePasswordDialog.setTitle("Change Password?");

        changePasswordDialog.setMessage("Are you Sure you want to change your password?");
        changePasswordDialog.setCancelable(false);
        changePasswordDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeUserPassword.sendResetPasswordEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        });
        changePasswordDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        changePasswordDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == GALLERY_IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
            String userImage = data.getData().toString();
            SharedPreferencesStorage.getSharedPrefInstance(getActivity()).saveUserImage( userImage);
//            userProfileView
//            userProfileView.setImageURI(data.getData());
//            userProfileView.setImageResource(R.drawable.certificatetrade);
//            Uri uri = data.getData();
            String userPic = SharedPreferencesStorage.getSharedPrefInstance(getActivity()).getUserImages(Constants.IMPEX_USER_IMAGE);
//            Picasso.get().load(userPic).into(userProfileView);

              Glide.with(this)
                .asBitmap()
                .load(Uri.parse(userPic))
                .into(userProfileView);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void passwordResetEmailSent(){
        Toast.makeText(getActivity(), "The password reset email has being sent, check your email to change", Toast.LENGTH_LONG).show();
    }
}

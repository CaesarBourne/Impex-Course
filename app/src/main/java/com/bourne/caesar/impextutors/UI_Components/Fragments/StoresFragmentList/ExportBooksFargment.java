package com.bourne.caesar.impextutors.UI_Components.Fragments.StoresFragmentList;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bourne.caesar.impextutors.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExportBooksFargment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExportBooksFargment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ExportBooksFargment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ExportBooksFargment newInstance(String param1, String param2) {
        ExportBooksFargment fragment = new ExportBooksFargment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_export_books_fargment, container, false);
    }

}

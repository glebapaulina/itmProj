package com.uwm.wmii.student.michal.itmproj.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.uwm.wmii.student.michal.itmproj.AlkoPickerActivity;
import com.uwm.wmii.student.michal.itmproj.ButtonGameActivity;
import com.uwm.wmii.student.michal.itmproj.GamesActivity;
import com.uwm.wmii.student.michal.itmproj.MainActivity;

import com.uwm.wmii.student.michal.itmproj.R;
import com.uwm.wmii.student.michal.itmproj.alkoninja.AlkoNinjaLauncher;
import com.uwm.wmii.student.michal.itmproj.singletons.AppLoginManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private AppLoginManager appLoginManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        appLoginManager = AppLoginManager.getInstance(getContext());

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        View v = inflater.inflate(R.layout.add_alkohol_dialog, container, false);
        Button homeButtonTest = (Button) view.findViewById(R.id.homeButton1);
        Button homeButtonAl = (Button) view.findViewById(R.id.homeButton2);
        Button homeButtonGames = (Button) view.findViewById(R.id.homeButton3);

        homeButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (appLoginManager.czyUzytkownikZalogowany()) {
                ((MainActivity)getActivity()).showAddAlkoholDialog();
            } else {
                appLoginManager.przejdzDoEkranuLogowaniaZKomunikatem();
            }
            }
        });

        homeButtonAl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (appLoginManager.czyUzytkownikZalogowany()) {
                Intent i = new Intent(getActivity(), AlkoPickerActivity.class);
                i.putExtra("fromTest", false);
                startActivity(i);
            } else {
                appLoginManager.przejdzDoEkranuLogowaniaZKomunikatem();
            }
            }
        });
        homeButtonGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), GamesActivity.class));
            }
        });
        return view;
    }


}

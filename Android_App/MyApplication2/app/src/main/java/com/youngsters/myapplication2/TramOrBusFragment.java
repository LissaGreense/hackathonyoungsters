package com.youngsters.myapplication2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;



public class TramOrBusFragment extends Fragment {

    View view;
    boolean isBus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.popup, container, false);

        isBus = false;

        ImageButton tram  = (ImageButton) view.findViewById(R.id.tram);
        ImageButton bus  = (ImageButton) view.findViewById(R.id.bus);

        tram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBus = false;
                MainActivity.isBus = false;
                getFragmentManager().beginTransaction().replace(R.id.content_main, new MainFragment()).addToBackStack("mainFrag").commit();
            }
        });

        bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBus = true;
                MainActivity.isBus = true;
                getFragmentManager().beginTransaction().replace(R.id.content_main, new MainFragment()).addToBackStack("mainFrag").commit();
            }
        });

        return view;
    }
}

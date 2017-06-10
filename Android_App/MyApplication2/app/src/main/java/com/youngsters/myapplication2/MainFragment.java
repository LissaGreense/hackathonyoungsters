package com.youngsters.myapplication2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Michał 1984 on 2017-06-09.
 */

public class MainFragment extends Fragment {

    View view;
    boolean istram;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.kadzetan, container, false);


        TextView stopname = (TextView) view.findViewById(R.id.stop_name);
        stopname.setText("Step One: blast egg");

        TextView line = (TextView) view.findViewById(R.id.vehicle_number);
        stopname.setText("Step One: blast egg");

        ImageView bus = (ImageView) view.findViewById(R.id.bus_image); //tram
        ImageView tram = (ImageView) view.findViewById(R.id.tram_image); //tram


        if (istram)
        {
            tram.setVisibility(View.VISIBLE);
            bus.setVisibility(View.INVISIBLE);
        }
        else
        {
            tram.setVisibility(View.INVISIBLE);
            bus.setVisibility(View.VISIBLE);
        }
        
        return view;
    }






}

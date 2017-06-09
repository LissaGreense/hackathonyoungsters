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
        view = inflater.inflate(R.layout.fragment_main, container, false);


        TextView stopname = (TextView) view.findViewById(R.id.textView2);
        stopname.setText("Step One: blast egg");

        TextView line = (TextView) view.findViewById(R.id.textView3);
        stopname.setText("Step One: blast egg");

        ImageView bus = (ImageView) view.findViewById(R.id.imageView4); //tram
        ImageView tram = (ImageView) view.findViewById(R.id.imageView3); //tram


        if (istram == true)
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

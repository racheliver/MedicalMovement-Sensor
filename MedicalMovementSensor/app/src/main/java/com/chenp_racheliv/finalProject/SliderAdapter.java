package com.chenp_racheliv.finalProject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    public int[] slide_images = {
            R.drawable.rest,
            R.drawable.half_rest,
            R.drawable.movement
    };

    public String[] slide_headlines = {
            "REST",
            "HALF REST",
            "MOVEMENT"
    };

    public String[] slide_texts = {
            "Measuring tremor during rest.\n"+"Sit firmly on a chair in front of a table, place your hand on the device while it's on the table.",
            "Measuring tremor during \"half rest\".\n"+"Sit firmly on a chair in front of a table, place your elbow on the table as you hold the device in your hand (the device should be held up).",
            "Measuring tremor during movement.\n"+"Sit firmly on a chair, reach forward and hold the device in your hand (without leaning and without any help)."
    };

    @Override
    public int getCount() {
        return slide_headlines.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout)object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = (ImageView)view.findViewById(R.id.slideImageID);
        TextView slideHeadlineView = (TextView)view.findViewById(R.id.slideHeadlineID);
        TextView slideTextView = (TextView)view.findViewById(R.id.slideTextID);

        slideImageView.setImageResource(slide_images[position]);
        slideHeadlineView.setText(slide_headlines[position]);
        slideTextView.setText(slide_texts[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}

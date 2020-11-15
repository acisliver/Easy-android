package com.example.easy_written;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.util.List;


public class SliderAdaper extends RecyclerView.Adapter<com.example.easy_written.SliderAdaper.SliderViewHoder>{
    private List<SlideItem> slideItems;
    private ViewPager2 viewPager2;
    public interface OnItemClickListener
    {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.mListener = listener;
    }

    SliderAdaper(List<SlideItem> slideItems, ViewPager2 viewPager2) {
        this.slideItems = slideItems;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public SliderViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHoder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_item_container,
                        parent,
                        false)
        );
    }

    @Override

    public void onBindViewHolder(@NonNull SliderViewHoder holder, int position) {
        holder.setImage(slideItems.get(position));
    }


    @Override
    public int getItemCount() {
        return slideItems.size();
    }

    class SliderViewHoder extends RecyclerView.ViewHolder{
        private RoundedImageView imageView;

        SliderViewHoder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageSlide);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos=getAdapterPosition();
                    if(pos!= RecyclerView.NO_POSITION){
                        mListener.onItemClick(view, pos);
                    }
                }
            });

        }
        void setImage(SlideItem slideItem){
            imageView.setImageURI(Uri.fromFile(new File(slideItem.getImage())));
        }
    }
}

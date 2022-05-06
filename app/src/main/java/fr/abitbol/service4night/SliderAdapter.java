package fr.abitbol.service4night;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private List<SliderItem> sliderItems;
    private ViewPager2 viewPager2;
    SliderAdapter(List<SliderItem> _sliderItems, ViewPager2 _viewPager2) {
        this.sliderItems = _sliderItems;
        this.viewPager2 = _viewPager2;
    }
    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.image_view_container, parent, false
                ) );
    }
    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.setImage(sliderItems.get(position));
//        if (position == sliderItems.size()- 2){
//            viewPager2.post(runnable);
//        }
    }
    @Override
    public int getItemCount() {
        return sliderItems.size();
    }
    class SliderViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_slider);
        }
        void setImage(SliderItem sliderItems){
            //use glide or picasso in case you get image from internet
            imageView.setImageBitmap(sliderItems.getImage());
        }
    }
}



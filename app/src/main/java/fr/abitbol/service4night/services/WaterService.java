package fr.abitbol.service4night.services;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WaterService extends Service {
    private boolean drinkable;
    private double price;
    public static final String NAME = "Water";
    public static final String ATTRIBUTE_1_NAME = "price";
    public static final Class<?> ATTRIBUTE_1_TYPE = Double.TYPE;
    public static final String ATTRIBUTE_2_NAME = "drinkable";
    public static final Class<?> ATTRIBUTE_2_TYPE = Boolean.TYPE;
    public static final int ATTRIBUTE_COUNT = 1;
    public static final ServiceParameters PARAMETERS = new ServiceParameters()
            .insertParameter(ATTRIBUTE_1_NAME,ATTRIBUTE_1_TYPE);
    public WaterService(double _price, boolean _drinkable) {
        super(NAME);
        price = _price;
        drinkable = _drinkable;
    }
    public WaterService(boolean _drinkable){
        super(NAME);
        drinkable = _drinkable;
        price = 0;
    }

    public boolean isDrinkable() {
        return drinkable;
    }

    public void setDrinkable(boolean drinkable) {
        this.drinkable = drinkable;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }



    @Override
    public boolean matchFilter(Service filter) {
        if (!(filter instanceof WaterService)){
            return false;
        }
        if (((WaterService) filter).isDrinkable() != drinkable){
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!( super.equals(obj))){
            return false;
        }
        return ((WaterService) obj).drinkable == drinkable;
    }
    @Override
    public Map<Object, Object> getAsMap() {
        Map<Object,Object> map = new HashMap<>();
        map.put(ATTRIBUTE_2_NAME,drinkable);
        map.put(ATTRIBUTE_1_NAME,price);
        return map;
    }
    @Override
    public Bundle getAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putDouble(ATTRIBUTE_1_NAME,price);
        bundle.putBoolean(ATTRIBUTE_2_NAME,drinkable);
        return bundle;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Log.i("WaterService logging", "writeToParcel: called ");
        parcel.writeBundle(getAsBundle());
    }
    public static final Parcelable.Creator<WaterService> CREATOR
            = new Parcelable.Creator<WaterService>() {
        @Override
        public WaterService createFromParcel(Parcel parcel) {
            Log.i("WaterService logging", "createFromParcel called ");
            return new WaterService((double) parcel.readBundle(getClass().getClassLoader()).getDouble(ATTRIBUTE_1_NAME),(Boolean) parcel.readBundle(getClass().getClassLoader()).getBoolean(ATTRIBUTE_2_NAME));
        }

        @Override
        public WaterService[] newArray(int i) {
            return new WaterService[i];
        }
    };
}

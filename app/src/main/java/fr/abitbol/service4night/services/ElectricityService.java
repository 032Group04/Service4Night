/*
 * Nom de classe : ElectricityService
 *
 * Description   : implémentation de Service pour le raccordement électrique
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.services;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ElectricityService extends Service {
    private double price;
    public static final String NAME = "Electricity";
    public static final String ATTRIBUTE_1_NAME = "price";
    public static final Class<?> ATTRIBUTE_1_TYPE = Double.TYPE;
    public static final int ATTRIBUTE_COUNT = 1;
    public static final ServiceParameters PARAMETERS = new ServiceParameters()
            .insertParameter(ATTRIBUTE_1_NAME,ATTRIBUTE_1_TYPE);
    public ElectricityService(double _price) {
        super(NAME);
        price = _price;
    }
    public ElectricityService(){
        super(ELECTRICITY_SERVICE);
        price = 0;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean matchFilter(Service filter) {
        return filter instanceof ElectricityService;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);

    }
    @Override
    public Map<Object, Object> getAsMap() {
        Map<Object,Object> map = new HashMap<>();
        map.put("price",price);
        return map;
    }
    @Override
    public Bundle getAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putDouble(ATTRIBUTE_1_NAME,price);
        return bundle;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(getAsBundle());
    }
    public static final Parcelable.Creator<ElectricityService> CREATOR
            = new Parcelable.Creator<ElectricityService>() {
        @Override
        public ElectricityService createFromParcel(Parcel parcel) {
            Log.i("ElectricityService logging", "createFromParcel: ");
            return new ElectricityService((double) parcel.readBundle(getClass().getClassLoader()).getDouble(ATTRIBUTE_1_NAME));
        }

        @Override
        public ElectricityService[] newArray(int i) {
            return new ElectricityService[i];
        }
    };
}

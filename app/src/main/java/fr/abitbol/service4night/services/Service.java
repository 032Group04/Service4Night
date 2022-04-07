package fr.abitbol.service4night.services;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.abitbol.service4night.Location;

public abstract class Service {

    public static final String WATER_SERVICE = "Water";
    public static final String ELECTRICITY_SERVICE = "Electricity";
    public static final String DUMPSTER_SERVICE = "Dumpster";
    public static final String INTERNET_SERVICE = "Internet";
    public static final String DRAINAGE_SERVICE = "Drainage";
    public static final String BATHROOM_SERVICE = "Bathroom";
    private String label;
    public Service(String _label){
        label = _label;


    }

    public String getLabel() {
        return label;
    }
    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj){
            return true;
        }
        if (!(obj.getClass().isAssignableFrom(Service.class) )){
            return false;
        }
        else{
            return label.equals(((Service) obj).getLabel());
        }


    }

    @NonNull
    @Override
    public String toString() {
        return (label + " service type.");
    }


}

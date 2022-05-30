/*
 * Nom de classe : DrainService
 *
 * Description   : implémentation de Service pour les vidanges d'eau sale
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

public class DrainService extends Service {
    public static final String NAME = "Drainage";
    private boolean blackWater;
    public static final String ATTRIBUTE_1_NAME = "blackWater";
    public static final Class<?> ATTRIBUTE_1_TYPE = Boolean.TYPE;
    public static final int ATTRIBUTE_COUNT = 1;
    public static final ServiceParameters PARAMETERS = new ServiceParameters()
            .insertParameter(ATTRIBUTE_1_NAME,ATTRIBUTE_1_TYPE);
    public DrainService(boolean _blackWater) {
        super(NAME);
        blackWater = _blackWater;
    }

    public boolean isBlackWater() {
        return blackWater;
    }

    @Override
    public boolean matchFilter(Service filter) {
        if (!(filter instanceof DrainService)){
            return false;
        }
        return ((DrainService) filter).isBlackWater() == blackWater;
    }

    @Override
    public Map<Object, Object> getAsMap() {
        Map<Object,Object> map = new HashMap<>();
        map.put("blackWater",blackWater);
        return map;
    }
    @Override
    public Bundle getAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ATTRIBUTE_1_NAME,blackWater);
        return bundle;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!( super.equals(obj))){
            return false;
        }
        return ((DrainService) obj).blackWater == blackWater;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBundle(getAsBundle());
    }
    public static final Parcelable.Creator<DrainService> CREATOR
            = new Parcelable.Creator<DrainService>() {
        @Override
        public DrainService createFromParcel(Parcel parcel) {
            Log.i("DrainService logging", "createFromParcel: ");
            return new DrainService((Boolean) parcel.readBundle(getClass().getClassLoader()).getBoolean(ATTRIBUTE_1_NAME));
        }

        @Override
        public DrainService[] newArray(int i) {
            return new DrainService[i];
        }
    };
}

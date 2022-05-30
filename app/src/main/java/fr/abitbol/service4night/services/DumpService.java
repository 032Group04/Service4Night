/*
 * Nom de classe : DumpService
 *
 * Description   : implémentation de Service pour les poubelles
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

public class DumpService extends Service {
    public static final String NAME = "Dumpster";

    public static final int ATTRIBUTE_COUNT = 0;
    public static final ServiceParameters PARAMETERS = null;

    public DumpService(){
        super(NAME);
    }


    @Override
    public boolean matchFilter(Service filter) {
        return filter instanceof DumpService;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
    @Override
    public Map<Object, Object> getAsMap() {
        Map<Object,Object> map = new HashMap<>();
        return map;
    }
    @Override
    public Bundle getAsBundle() {
        Bundle bundle = new Bundle();
        //bundle.putString("label",NAME);
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
    public static final Parcelable.Creator<DumpService> CREATOR
            = new Parcelable.Creator<DumpService>() {
        @Override
        public DumpService createFromParcel(Parcel parcel) {
            Log.i("DumpService logging", "createFromParcel: ");
            Bundle bundle = parcel.readBundle(getClass().getClassLoader());
            return new DumpService();
        }

        @Override
        public DumpService[] newArray(int i) {
            return new DumpService[i];
        }
    };
}

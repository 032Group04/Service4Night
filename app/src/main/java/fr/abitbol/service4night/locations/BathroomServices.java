/*
 * Nom de classe : BathroomService
 *
 * Description   : implémentation de Service pour les toilettes/douches (non utilisé à ce jour)
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.locations;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class BathroomServices extends Service{
    private boolean shower;
    public static final String NAME = "Bathroom";
    public static final String ATTRIBUTE_1_NAME = "shower";
    public static final Class<?> ATTRIBUTE_1_TYPE = Boolean.TYPE;
    public static final int ATTRIBUTE_COUNT = 1;
    public static ServiceParameters PARAMETERS = new ServiceParameters()
            .insertParameter(ATTRIBUTE_1_NAME,ATTRIBUTE_1_TYPE);
    public BathroomServices(boolean _shower) {
        super(NAME);
        shower = _shower;
    }

    @Override
    public boolean matchFilter(Service filter) {
        return false;
    }

    @Override
    public Map<Object, Object> getAsMap() {
        Map<Object,Object> map = new HashMap<>();
        map.put((String)"shower",(boolean) shower);
        return map;
    }

    @Override
    public Bundle getAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ATTRIBUTE_1_NAME,shower);
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
    public static final Parcelable.Creator<BathroomServices> CREATOR
            = new Parcelable.Creator<BathroomServices>() {
        @Override
        public BathroomServices createFromParcel(Parcel parcel) {
            return new BathroomServices((Boolean)parcel.readBundle().get(ATTRIBUTE_1_NAME) );
        }

        @Override
        public BathroomServices[] newArray(int i) {
            return new BathroomServices[i];
        }
    };
}

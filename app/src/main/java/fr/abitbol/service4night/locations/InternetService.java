/*
 * Nom de classe : InternetService
 *
 * Description   : implémentation de Service pour les connexions internet
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
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InternetService extends Service{
    public enum ConnectionType{
        public_wifi, private_provider;
        public static final Class<ConnectionType> TYPE = ConnectionType.class;
    }
    private ConnectionType connectionType;
    private double price;
    public static final String NAME = "Internet";
    public static final String ATTRIBUTE_1_NAME = "connectionType";
    public static final Class<?> ATTRIBUTE_1_TYPE = ConnectionType.TYPE;
    public static final String ATTRIBUTE_2_NAME = "price";
    public static final Class<?> ATTRIBUTE_2_TYPE = Double.TYPE;
    public static final int ATTRIBUTE_COUNT = 2;
    public static final ServiceParameters PARAMETERS = new ServiceParameters()
            .insertParameter(ATTRIBUTE_1_NAME,ATTRIBUTE_1_TYPE)
            .insertParameter(ATTRIBUTE_2_NAME,ATTRIBUTE_2_TYPE);
    public InternetService(ConnectionType type, double _price){
       super(NAME);
       connectionType = type;
       price = _price;

   }
    public InternetService(ConnectionType type){
        super(INTERNET_SERVICE);
        connectionType = type;
        price = 0;

    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean matchFilter(Service filter) {
        return filter instanceof InternetService;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!( super.equals(obj))){
            return false;
        }
        return ((InternetService) obj).connectionType == connectionType;
    }
    @Override
    public Map<Object, Object> getAsMap() {
        Map<Object,Object> map = new HashMap<>();
        map.put("connectionType",connectionType.name());
        map.put("price", price);
        return map;
    }
    @Override
    public Bundle getAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(ATTRIBUTE_1_NAME,connectionType.name());
        bundle.putDouble(ATTRIBUTE_2_NAME,price);
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
    public static final Parcelable.Creator<InternetService> CREATOR
            = new Parcelable.Creator<InternetService>() {
        @Override
        public InternetService createFromParcel(Parcel parcel) {
            Log.i("Internet service logging", "createFromParcel: ");
            return new InternetService(ConnectionType.valueOf((String) parcel.readBundle(getClass().getClassLoader()).getString(ATTRIBUTE_1_NAME)),
                    (double) parcel.readBundle(getClass().getClassLoader()).getDouble(ATTRIBUTE_2_NAME));
        }

        @Override
        public InternetService[] newArray(int i) {
            return new InternetService[i];
        }
    };
}

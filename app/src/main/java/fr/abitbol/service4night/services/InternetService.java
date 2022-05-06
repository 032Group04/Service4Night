package fr.abitbol.service4night.services;

import androidx.annotation.Nullable;

public class InternetService extends Service{
    public enum ConnectionType{public_wifi, private_provider}
    private ConnectionType connectionType;
    private double price;
    public static final String NAME = "Internet";
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
}

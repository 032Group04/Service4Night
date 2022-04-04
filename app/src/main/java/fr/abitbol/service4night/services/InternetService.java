package fr.abitbol.service4night.services;

public class InternetService extends Service{
    enum ConnectionType{public_wifi, private_provider}
    private ConnectionType connectionType;
    private float price;

    public InternetService(ConnectionType type, float _price){
       super(INTERNET_SERVICE);
       connectionType = type;
       price = _price;

   }
}

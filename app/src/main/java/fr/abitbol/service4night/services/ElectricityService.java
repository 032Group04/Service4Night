package fr.abitbol.service4night.services;

public class ElectricityService extends Service {
    private float price;
    public ElectricityService(float _price) {
        super(ELECTRICITY_SERVICE);
        price = _price;
    }
    public ElectricityService(){
        super(ELECTRICITY_SERVICE);
        price = 0;
    }
}

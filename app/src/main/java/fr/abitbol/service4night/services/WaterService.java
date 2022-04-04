package fr.abitbol.service4night.services;

public class WaterService extends Service {
    private boolean drinkable;
    private float price;
    public WaterService(float _price, boolean _drinkable) {
        super(WATER_SERVICE);
        price = _price;
        drinkable = _drinkable;
    }
}

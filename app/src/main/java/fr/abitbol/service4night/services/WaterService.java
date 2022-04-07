package fr.abitbol.service4night.services;

public class WaterService extends Service {
    private boolean drinkable;
    private float price;
    public WaterService(float _price, boolean _drinkable) {
        super(WATER_SERVICE);
        price = _price;
        drinkable = _drinkable;
    }
    public WaterService(boolean _drinkable){
        super(WATER_SERVICE);
        drinkable = _drinkable;
        price = 0;
    }

    public boolean isDrinkable() {
        return drinkable;
    }

    public void setDrinkable(boolean drinkable) {
        this.drinkable = drinkable;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}

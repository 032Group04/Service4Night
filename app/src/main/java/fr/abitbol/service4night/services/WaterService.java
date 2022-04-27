package fr.abitbol.service4night.services;

public class WaterService extends Service {
    private boolean drinkable;
    private double price;
    public static final String NAME = "Water";
    public WaterService(double _price, boolean _drinkable) {
        super(NAME);
        price = _price;
        drinkable = _drinkable;
    }
    public WaterService(boolean _drinkable){
        super(NAME);
        drinkable = _drinkable;
        price = 0;
    }

    public boolean isDrinkable() {
        return drinkable;
    }

    public void setDrinkable(boolean drinkable) {
        this.drinkable = drinkable;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

package fr.abitbol.service4night.services;

public class ElectricityService extends Service {
    private double price;
    public static final String NAME = "Electricity";
    public ElectricityService(double _price) {
        super(NAME);
        price = _price;
    }
    public ElectricityService(){
        super(ELECTRICITY_SERVICE);
        price = 0;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

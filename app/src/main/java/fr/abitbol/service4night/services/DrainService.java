package fr.abitbol.service4night.services;

public class DrainService extends Service {
    enum DrainageType{ grey_water, black_water}
    private DrainageType drainageType;

    public DrainService(DrainageType type) {
        super(DRAINAGE_SERVICE);
        drainageType = type;
    }
}

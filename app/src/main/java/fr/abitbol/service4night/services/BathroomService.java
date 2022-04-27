package fr.abitbol.service4night.services;

public class BathroomService extends Service{
    private boolean shower;
    public static final String NAME = "Bathroom";
    public BathroomService(boolean _shower) {
        super(NAME);
        shower = _shower;
    }
}

package fr.abitbol.service4night.services;

public class BathroomService extends Service{
    private boolean shower;

    public BathroomService(boolean _shower) {
        super(BATHROOM_SERVICE);
        shower = _shower;
    }
}

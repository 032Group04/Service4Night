package fr.abitbol.service4night;

import java.util.Map;

public class LocationBuilder {


    public static Location build(Map<String, Object> data){
        return new Location((double)data.get("latitude"),(double)data.get("longitude"),(String) data.get("description"));
    }
}

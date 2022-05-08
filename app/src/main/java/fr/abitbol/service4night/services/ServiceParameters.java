package fr.abitbol.service4night.services;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceParameters {
    private LinkedHashMap <String,Class<?>> parameters;

    public ServiceParameters(){
        parameters = new LinkedHashMap<>();
    }
    public ServiceParameters insertParameter(String name,Class<?> clazz){
        parameters.put(name,clazz);
        return this;
    }
    public Class<?>[] getParametersTypes(){
        return parameters.values().toArray(new Class<?>[parameters.size()]);
    }
    public String[] getParametersNames(){
        return parameters.keySet().toArray(new String[parameters.size()]);
    }
}

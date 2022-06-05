/*
 * Nom de classe : ServiceParameters
 *
 * Description   : classe dédiée à la reflectivité (probable utilisation future pour générifier la reconstruction des objets Service depuis firestore)
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.locations;

import java.util.LinkedHashMap;

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

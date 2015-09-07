package com.worldspotlightapp.android.maincontroller.modules.citymodule;

import com.worldspotlightapp.android.model.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * Created by jiahaoliuliu on 15/9/7.
 */
public class CityModuleObservable extends AbstractCityModuleObservable {

    /**
     * The list of cities
     */
    private List<City> mCitiesList;

    /**
     * Empty constructor
     */
    public CityModuleObservable() {
        this.mCitiesList = new ArrayList<City>();
    }

    @Override
    public void retrieveCitiesList(Observer observer) {
        // TODO: Implement this
    }
}

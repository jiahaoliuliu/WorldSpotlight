package com.worldspotlightapp.android.maincontroller.modules.citymodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.City;
import com.worldspotlightapp.android.model.UserData;

import java.util.List;
import java.util.Set;

/**
 * Created by jiahaoliuliu on 2/20/15.
 */
public class CityModuleCitiesSetResponse extends BaseModuleResponse {

    private Set<City> mCitiesSet;

    public CityModuleCitiesSetResponse(ParseResponse parseResponse, Set<City> citiesSet) {
        super(parseResponse);
        this.mCitiesSet = citiesSet;
    }

    public Set<City> getCitiesSet() {
        return mCitiesSet;
    }
}

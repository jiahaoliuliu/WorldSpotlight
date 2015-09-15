package com.worldspotlightapp.android.maincontroller.modules.citymodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.City;
import com.worldspotlightapp.android.model.UserData;

import java.util.List;

/**
 * Created by jiahaoliuliu on 2/20/15.
 */
public class CityModuleCitiesListResponse extends BaseModuleResponse {

    private List<City> mCitiesList;

    public CityModuleCitiesListResponse(ParseResponse parseResponse, List<City> citiesList) {
        super(parseResponse);
        this.mCitiesList = citiesList;
    }

    public List<City> getCitiesList() {
        return mCitiesList;
    }
}

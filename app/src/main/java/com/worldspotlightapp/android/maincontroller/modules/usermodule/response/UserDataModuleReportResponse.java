package com.worldspotlightapp.android.maincontroller.modules.usermodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Like;
import com.worldspotlightapp.android.model.Report;

/**
 * Special class created to notify video report.
 * Created by jiahaoliuliu on 2/20/15.
 */
public class UserDataModuleReportResponse extends BaseModuleResponse {

    private Report mReport;

    public UserDataModuleReportResponse(ParseResponse parseResponse, Report report) {
        super(parseResponse);
        this.mReport = report;
    }

    public Report getReport() {
        return mReport;
    }
}
package org.cghr.hcamp.service.grid

import org.cghr.commons.db.DbAccess
import org.cghr.dataViewModel.DataModelUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Created by ravitej on 8/4/14.
 */
@Controller
@RequestMapping("/GridService/report")
class ReportService {


    @Autowired
    DataModelUtil dataModelUtil
    @Autowired
    DbAccess dbAccess


    def sql = ""

    @RequestMapping(value = "/{reportId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String getReport(@PathVariable("reportId") int reportId) {

        Integer wrkstn = reportId - 10
        sql = "select * from wrkstn$wrkstn"

        constructJsonResponse(sql, [])

    }
    // Creating a Json from sql Query
    String constructJsonResponse(String sql, List params) {


        def filtersArray = dbAccess.getColumnLabels(sql, params).collect {
            '#text_filter'
        }
        def sortingArray = dbAccess.getColumnLabels(sql, params).collect {
            'str'
        }

        return dataModelUtil.constructJsonResponse(sql, params, filtersArray.join(","), sortingArray.join(","));

    }


}

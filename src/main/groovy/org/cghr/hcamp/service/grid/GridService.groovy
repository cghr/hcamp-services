package org.cghr.hcamp.service.grid

import com.github.jknack.handlebars.Handlebars
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
@RequestMapping("/GridService")
class GridService {

    @Autowired
    DataModelUtil dataModelUtil
    @Autowired
    DbAccess dbAccess

    String sql = ""


    @RequestMapping(value = "/hcamp/wrkstn/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getWorkStation1(@PathVariable("id") Integer id) {


        Map data = [nextState: "hcamp.wrkstn" + id + "Participant", entityId: 'memberId',column:'a.memberId', refs: [:]]
        String link = createLink(data)
        Integer next = (id + 1)
        Integer prev = (id - 1)

        if (id != 1)
            sql = "SELECT $link,name,gender,age,a.memberId FROM member a  JOIN wrkstn${prev} b ON a.memberId=b.memberId LEFT JOIN wrkstn${id} c ON b.memberId=c.memberId WHERE a.age>29 AND a.age<71 AND c.memberId IS NULL"
        else
            sql = "select $link,name,gender,age,a.memberId from member a LEFT JOIN wrkstn${id} b ON a.memberId=b.memberId WHERE a.age>29 AND a.age<71 AND  b.memberId IS NULL".toString()

        return constructJsonResponse(sql, []);

    }

    // Creating a Json from sql Query
    String constructJsonResponse(String sql, List params) {


        def filtersArray = dbAccess.getColumnLabels(sql, params).split(",").collect {
            "#text_filter"
        }

        def sortingArray = dbAccess.getColumnLabels(sql, params).split(",").collect {
            "str"
        }

        return dataModelUtil.constructJsonResponse(sql, params, filtersArray.join(","), sortingArray.join(","));

    }

    String createLink(Map contextData) {

        Handlebars handlebars = new Handlebars()
        Map entities = contextData.refs
        List entityList = []
        entities.each { key, value ->

            entityList << "$key" + ":" + "$value".toString()

        }
        def refs = entityList.join(",")

        def template = ""
        if (entityList.isEmpty())
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{column}},'})\">',{{column}},'</a>') AS CHAR) id".toString()
        else
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{column}},',$refs })\">',{{column}},'</a>') AS CHAR) id".toString()

        def compiledTemplate = handlebars.compileInline(template)
        compiledTemplate.apply(contextData)


    }


}

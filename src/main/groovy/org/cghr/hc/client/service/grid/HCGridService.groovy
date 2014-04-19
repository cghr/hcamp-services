package org.cghr.hc.client.service.grid
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
@RequestMapping("/GridService/hc")
class HCGridService {

    @Autowired
    DataModelUtil dataModelUtil
    @Autowired
    DbAccess dbAccess

    def sql = ""


    //Areas
    @RequestMapping(value = "/area", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String getAreas() {

        Map data=[nextState:'hc.areaDetail.house',entityId:'areaId',refs:[:]]
        String link=createLink(data)
        sql = "select $link,name,landmark,pincode from area".toString()

        return constructJsonResponse(sql,[])
    }

    //Houses
    @RequestMapping(value = "/area/{areaId}/house", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String getHouses(@PathVariable("areaId") Integer areaId) {

        Map data=[nextState: 'hc.houseDetail.household',entityId:'houseId',refs: [areaId: areaId]]
        String link=createLink(data)
        sql = "select $link,houseNs,gps_latitude,gps_longitude from house where areaId=?".toString()

        return constructJsonResponse(sql,[areaId])
    }

    //Households
    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String getHouseholds(@PathVariable("areaId") Integer areaId,@PathVariable("houseId") Integer houseId) {

        Map data=[nextState: 'hc.householdDetail.visit',entityId:'householdId',refs: [areaId: areaId, houseId: houseId]]
        String link=createLink(data)
        sql = "select $link,mobile1,mobile2 from household where houseId=?".toString()

        return constructJsonResponse(sql,[houseId])
    }

   //Head of the Household
    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/head", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String getHead(@PathVariable("areaId") Integer areaId,@PathVariable("houseId") Integer houseId,@PathVariable("householdId") Integer householdId) {

        Map data=[nextState: 'hc.memberDetail',entityId:'memberId',refs: [areaId: areaId, houseId: houseId,householdId: householdId]]
        String link=createLink(data)
        sql = "select $link,name,gender,age from member where  householdId=? and relationship='self'".toString()

        return constructJsonResponse(sql,[householdId])
    }

    //Members
    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/member", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String getMembers(@PathVariable("areaId") Integer areaId,@PathVariable("houseId") Integer houseId,@PathVariable("householdId") Integer householdId) {

        Map data=[nextState: 'hc.memberDetail.basicInf',entityId:'memberId',refs:[areaId: areaId, houseId: houseId,householdId:householdId]]
        String link=createLink(data)
        sql = "select $link,name,gender,age from member where  householdId=?".toString()

        return constructJsonResponse(sql,[householdId])
    }
    //FFQ
    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/ffq", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String getFFq(@PathVariable("areaId") Integer areaId,@PathVariable("houseId") Integer houseId,@PathVariable("householdId") Integer householdId) {

        Map data=[nextState: 'hc.ffqDetail.general',entityId:'memberId',refs:[areaId: areaId, houseId: houseId,householdId:householdId]]
        String link=createLink(data)
        sql = "select $link,name,gender,age from member where  householdId=?".toString()

        return constructJsonResponse(sql,[householdId])
    }

    //HC visit
    @RequestMapping(value = "/area/{areaId}/house/{houseId}/household/{householdId}/visit", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String getEnumVisits(@PathVariable("areaId") Integer areaId,@PathVariable("houseId") Integer houseId,@PathVariable("householdId") Integer householdId) {

        sql = "select id,hhVisit,time from hcVisit where householdId=? ".toString()
        return constructJsonResponse(sql,[householdId])
    }



    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    String get() {

        Map data=[nextState: '',entityId:'']
        String link=createLink(data)
        sql = "select $link from ".toString()

        return constructJsonResponse(sql,[])
    }





    // Creating a Json from sql Query
    String constructJsonResponse(String sql, List params) {


        def filtersArray=dbAccess.getColumnLabels(sql,params).collect{
            '#text_filter'
        }
        def sortingArray=dbAccess.getColumnLabels(sql,params).collect{
            'str'
        }

        return dataModelUtil.constructJsonResponse(sql, params,filtersArray.join(","), sortingArray.join(","));

    }

//    String createLink(Map contextData) {
//
//        Handlebars handlebars = new Handlebars()
//        String template = "CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{entityId}},'})\">',{{entityId}},'</a>') id"
//        def compiledTemplate = handlebars.compileInline(template)
//
//
//        compiledTemplate.apply(contextData)
//
//
//    }
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
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{entityId}},'})\">',{{entityId}},'</a>') AS CHAR) id".toString()
        else
            template = "CAST(CONCAT('<a ui-sref=\"{{nextState}}({ {{entityId}}:',{{entityId}},',$refs })\">',{{entityId}},'</a>') AS CHAR) id".toString()

        def compiledTemplate = handlebars.compileInline(template)
        compiledTemplate.apply(contextData)


    }


}

package org.cghr.hcamp.service.validation

import org.cghr.commons.db.DbAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletResponse

/**
 * Created by ravitej on 25/4/14.
 */
@Controller
@RequestMapping("/ValidationService")
class ValidationService {


    def sql = ""
    @Autowired
    DbAccess dbAccess

    @RequestMapping(value = "/wrkstn/{id}/{memberId}", method = RequestMethod.GET)
    @ResponseBody
    void isValidForWorkstation(
            @PathVariable("id") Integer id, @PathVariable("memberId") Long memberId, HttpServletResponse response) {


        Integer previousWrkStn = (id - 1)
        if(previousWrkStn==0)
            return
        println 'previous wrkstn '+previousWrkStn
        sql = "select count(*) count from wrkstn$previousWrkStn where memberId=?"
        int count = dbAccess.getRowAsMap(sql, [memberId]).count
        println count

        if (count != 1)
            response.sendError(403)
        else
            response.setStatus(200)

    }


}

package org.cghr.hcamp.service.rest

import groovy.sql.Sql
import org.cghr.commons.db.DbAccess
import org.cghr.commons.db.DbStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Created by ravitej on 14/5/14.
 */

@RestController
@RequestMapping("/rest")
class RestService {

    @Autowired
    DbAccess dbAccess
    @Autowired
    Sql gSql
    @Autowired
    DbStore dbStore

    @RequestMapping(value = "/wristBand/{wristBand}", method = RequestMethod.GET, produces = "application/json")
    Map getMemberIdFromWristBand(@PathVariable String wristBand) {

        dbAccess.getRowAsMap("select memberId from wrkstn1 where wristBand=?", [wristBand])

    }

    @RequestMapping(value = "/invitationCard/{invitationCard}", method = RequestMethod.GET, produces = "application/json")
    Map getMemberIdFromInvitationCard(@PathVariable("invitationCard") String invitationCard) {

        String phn = dbAccess.getRowAsMap("select phn from invitationCardPhn where barcode=?", [invitationCard]).phn
        println 'phn number ' + phn
        Map data = dbAccess.getRowAsMap("select memberId from invitationCard where phn=?", [phn])
        println data
        data

    }

    @RequestMapping(value = "/reportCard/{memberId}", method = RequestMethod.GET, produces = "application/json")
    Map getReportCardData(@PathVariable("memberId") String memberId) {

        dbAccess.getRowAsMap("select phn,name,age,gender,height,weight,bmi,bpSystolic,bpDiastolic,waistCircumference,bmi from member a  join  wrkstn2 b on a.memberId=b.memberId join wrkstn3 c on a.memberId=c.memberId" +
                " join wrkstn4 d on a.memberId=d.memberId join invitationCard e on a.memberId=e.memberId where a.memberId=?", [memberId])
    }

    @RequestMapping(value = "/reportCardStatus/{memberId}", method = RequestMethod.GET)
    String updateReportCardStatus(@PathVariable("memberId") String memberId) {

        dbStore.saveOrUpdate([status: 1, memberId: memberId], 'wrkstn6')
        return ''

    }

    @RequestMapping(value = "/participant/status/{wristBand}", method = RequestMethod.GET, produces = "application/json")
    Map getParticipantStatus(@PathVariable("wristBand") String wristBand) {

        String memberId = dbAccess.getRowAsMap("select * from wrkstn1 where wristBand=?", [wristBand]).memberId
        String sql = "select a.memberId wrkstn1,b.memberId wrkstn2,c.memberId wrkstn3,d.memberId wrkstn4,e.memberId wrkstn5,f.memberId wrkstn6 from wrkstn1 a " +
                "left join wrkstn2 b on a.memberId=b.memberId " +
                "left join wrkstn3 c on a.memberId=c.memberId " +
                "left join wrkstn4 d on a.memberId=d.memberId " +
                "left join wrkstn5 e on a.memberId=e.memberId " +
                "left join wrkstn6 f on a.memberId=f.memberId where a.memberId=?"
        dbAccess.getRowAsMap(sql, [memberId])


    }

    @RequestMapping(value = "/verification/{memberId}", method = RequestMethod.GET, produces = "application/json")
    Map getVerificationDetails(@PathVariable("memberId") String memberId) {

        String sql = "select spouseName,motherName from memberFamilyMedicalHistory a join memberBp1 b on a.memberId=b.memberId where a.memberId=?"
        dbAccess.getRowAsMap(sql, [memberId])

    }

    @RequestMapping(value = "/cleanUp", method = RequestMethod.GET)
    void cleanUp() {

        String[] tables = ["wrkstn1", "wrkstn2", "wrkstn3", "wrkstn4", "wrkstn5", "wrkstn6"];
        tables.each {
            gSql.execute("truncate table " + it)
        }
    }


}

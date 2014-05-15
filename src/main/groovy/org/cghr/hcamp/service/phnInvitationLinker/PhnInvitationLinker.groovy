package org.cghr.hcamp.service.phnInvitationLinker

import groovy.sql.Sql
import org.cghr.commons.db.DbAccess
import org.cghr.commons.db.DbStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Created by ravitej on 15/5/14.
 */
@RestController
@RequestMapping("/generatePhnInvitationLinks")
class PhnInvitationLinker {

    @Autowired
    DbStore dbStore
    @Autowired
    String barcodeLeadingZeros
    @Autowired
    DbAccess dbAccess
    @Autowired
    Sql gSql

    @RequestMapping(value = "/generate", method = RequestMethod.POST, consumes = 'application/json')
    String generate(@RequestBody Map<String, Long> config) {


        println config
        dbAccess.removeData(['invitationCardPhn'])

        barcodeLeadingZeros = (barcodeLeadingZeros == null) ? '0000' : barcodeLeadingZeros
        long barcode = config.invitationCardBarcodeStart
        for (long phn = config.phnStart; phn <= config.phnEnd; phn++) {
            //dbStore.saveOrUpdate([phn: phn, barcode: barcode], 'invitationCardPhn')
            gSql.execute("insert into invitationCardPhn(phn,barcode) values(?,?)", [phn, barcodeLeadingZeros + barcode])
            barcode++
        }

        println dbAccess.getRowsAsListOfMaps('select * from invitationCardPhn', [])
        return ''

    }

}

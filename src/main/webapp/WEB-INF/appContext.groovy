import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.cghr.chart.AngularChartDataModel
import org.cghr.commons.db.CleanUp
import org.cghr.commons.db.DbAccess
import org.cghr.commons.db.DbStore
import org.cghr.commons.file.FileSystemStore
import org.cghr.dataSync.commons.AgentProvider
import org.cghr.dataSync.commons.SyncRunner
import org.cghr.dataSync.service.SyncUtil
import org.cghr.dataViewModel.DataModelUtil
import org.cghr.dataViewModel.DhtmlxGridModelTransformer
import org.cghr.security.service.OnlineAuthService
import org.cghr.security.service.UserService
import org.cghr.startupTasks.DbImport
import org.cghr.startupTasks.DirCreator
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.commons.CommonsMultipartResolver

beans {
    xmlns([context: 'http://www.springframework.org/schema/context'])
    xmlns([mvc: 'http://www.springframework.org/schema/mvc'])

    //Common Services
    context.'component-scan'('base-package': 'org.cghr.commons.web.controller')
    context.'component-scan'('base-package': 'org.cghr.dataSync.controller')
    context.'component-scan'('base-package': 'org.cghr.security.controller')
    context.'component-scan'('base-package': 'org.cghr.survey.controller')

    context.'component-scan'('base-package': 'org.cghr.hcamp.service')

    mvc.'annotation-driven'()
    mvc.'interceptors'() {
        mvc.'mapping'('path': '/api/GridService/**') {
            bean('class': 'org.cghr.security.controller.AuthInterceptor')
        }
    }
    multiPartResolver(CommonsMultipartResolver)

    barcodeLeadingZeros(String,'0000')
    String userHome = System.getProperty('userHome')
    String appPath = System.getProperty('basePath')
    String server = 'http://barshi.vm-host.net:8080/hcServer/'
    serverBaseUrl(String, server)

    //Data Config
//    dataSource(DataSource) {
//        driverClassName = 'org.h2.Driver'
//        url = 'jdbc:h2:~/hcDemo;database_to_upper=false;mode=mysql'
//        username = 'sa'
//        password = ''
//        initialSize = 5
//        maxActive = 10
//        maxIdle = 5
//        minIdle = 2
//    }
    //Data Config
    //Data Config
    dataSource(DataSource) {
        driverClassName = 'org.h2.Driver'
        //url = 'jdbc:h2:tcp://localhost/~/hcDemo;database_to_upper=false;mode=mysql'
        url = 'jdbc:h2:~/hcDemo;database_to_upper=false;mode=mysql'
        username = 'sa'
        password = ''
        initialSize = 5
        maxActive = 10
        maxIdle = 5
        minIdle = 2
    }

    gSql(Sql, dataSource = dataSource)
    dbAccess(DbAccess, gSql = gSql)
    // Entities
    dataStoreFactory(HashMap, [ wrkstn1:'memberId', wrkstn2:'memberId', wrkstn3:'memberId', wrkstn4:'memberId', wrkstn5:'memberId', wrkstn6:'memberId', invitationCardPhn:'phn' ])
    dbStore(DbStore, gSql = gSql, dataStoreFactory = dataStoreFactory)

    // File Store Config
    fileStoreFactory(HashMap,
            [memberImage: [
                    memberConsent: userHome + "hcDemo/repo/images/consent",
                    memberPhotoId: userHome + "hcDemo/repo/images/photoId",
                    memberPhoto: userHome + "hcDemo/repo/images/photo"
            ]])
    fileSystemStore(FileSystemStore, fileStoreFactory = fileStoreFactory, dbStore = dbStore)

    //Data Model for reports
    transformer(DhtmlxGridModelTransformer, gSql = gSql)
    dataModelUtil(DataModelUtil, transformer = transformer, dbAccess = dbAccess)

    // Security
    serverAuthUrl(String, server + "api/security/auth")
    restTemplate(RestTemplate)
    onlineAuthService(OnlineAuthService, serverAuthUrl = serverAuthUrl, restTemplate = restTemplate)
    userService(UserService, dbAccess = dbAccess, dbStore = dbStore, onlineAuthService = onlineAuthService)

    //Startup Tasks
    dbImport(DbImport, sqlDir = appPath + 'sqlImport', gSql = gSql)
    dirCreator(DirCreator, [
            userHome + 'hcDemo/repo/images/consent',
            userHome + 'hcDemo/repo/images/photo',
            userHome + 'hcDemo/repo/images/photoId'
    ])

    //Data Synchronization
    syncUtil(SyncUtil, restTemplate = restTemplate, baseIp = '192.168.0.', startNode = 100, endNode = 120, port = 8080, pathToCheck = 'api/status/manager')
    agentProvider(AgentProvider, gSql = gSql, dbAccess = dbAccess, dbStore = dbStore, restTemplate = restTemplate, changelogChunkSize = 20,
            serverBaseUrl = serverBaseUrl,
            downloadInfoPath = 'api/sync/downloadInfo',
            downloadDataBatchPath = 'api/data/dataAccessBatchService/',
            uploadPath = 'api/data/dataStoreBatchService',
            awakeFileManagerPath = 'AwakeFileManager',
            fileStoreFactory = fileStoreFactory,
            userHome = userHome,
            syncUtil = syncUtil)
    syncRunner(SyncRunner, agentProvider = agentProvider)

    //Chart Data Model Services
    angularChartDataModel(AngularChartDataModel, dbAccess = dbAccess)

    // Maintenance Tasks
    cleanup(CleanUp, dbAccess = dbAccess, excludedEntities = 'user')

}
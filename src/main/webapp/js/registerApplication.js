import {
    Utils
} from 'symphony-integration-commons/js/utils.service';
import {
    initEnrichers,
    registerEnrichers
} from 'symphony-integration-commons/services/registerEnricher';
import _ from 'lodash';

const dependencies = [
    'ui',
    'extended-user-info',
    'modules',
    'entity',
    'dialogs',
    'links'
];

const params = {
    configurationId: Utils.getParameterByName('configurationId'),
    botUserId: Utils.getParameterByName('botUserId'),
    host: `${window.location.protocol}//${window.location.hostname}:${window.location.port}`
};

const registerExtension = (config) => {
    const controllerName = `${config.appId}:controller`;
    const uiService = SYMPHONY.services.subscribe('ui');
    const dialogsService = SYMPHONY.services.subscribe('dialogs');
    uiService.registerExtension(
        'app-settings',
        config.appId,
        controllerName, {
            label: 'Configure'
        }
    );
}


const registerModule = (config) => {
    const controllerName = `${config.appId}:controller`;
    const controllerService = SYMPHONY.services.subscribe(controllerName)
    
    const modulesService = SYMPHONY.services.subscribe('modules');
    const entityService = SYMPHONY.services.subscribe('entity');
    let msgType = 'com.symphony.integration.zapier.event.v2.searchMessage';
    entityService.registerRenderer(msgType, {}, controllerName);
    const chartMap = {
        'ibm': 'CREDIT.BOND.US459200HK05.PRICE',
        'google': 'CREDIT.BOND.US38259PAB85.PRICE',
        'apple': 'CREDIT.BOND.US037833CF55.PRICE'
    }
    controllerService.implement({
        action: _.throttle(function(data){
            const dialogsService = SYMPHONY.services.subscribe('dialogs');
            // dialogsService.show('testDialog', 'Chart', 'citibotDialogService', {}, {type: data});
            modulesService.show(
                config.appId, {
                    title: config.appTitle
                },
                controllerName,
                'https://uat.citivelocity.com/analytics/charting3/?allowCross=false', {
                    canFloat: true
                }
            );
            
        }, 300),
        render(type, data) {

            if (type == msgType) {
                
                let resultJson = JSON.parse(data.data).Results;
                let resultML = resultJson.map( article => {
                    return `
                    <card class="barStyle" accent="tempo-bg-color--green" iconSrc="http://rick-li.ngrok.io/citibot/apps/citibot/img/bigicons_bigicon_doc.svg.png">
                        <header>
                            <div>
                                <a class="tempo-text-color--link" href="www.google.com">${_.escape(article.docTitle)}</a>
                                    <span>Author</span>
                                    <span class="tempo-text-color--blue">${article.analyst.join(',')}</span> 
                            </div>
                        </header>
                        <body>
                        <div>
                            <span class="tempo-text-color--secondary">Description:</span>
                            <span class="tempo-text-color--normal">${_.escape(article.docTeaser)}</span>
                        <br/>
                        <img src="${article.coverImageURL}"/>
                    </div>
                    <hr/>
                    </body>
                    </card>`
                });
                
                return {template: `<messageML>${resultML.join('')}
                <card class="barStyle" accent="tempo-bg-color--green" iconSrc="http://rick-li.ngrok.io/citibot/apps/citibot/img/bigicons_bigicon_doc.svg.png">
                <header>
                    
                        Charting
                    
                </header>
                <body>
                    
                </body>
                </card>
            
                    <div><action id="charting" class="tempo-btn--primary"/></div>
                    </messageML>`, data: {
                        charting: {label: 'Charting', service: controllerName, data: {searchContent: 'ibm'}}
                }};
               
            }
        },
        link() {
        },
        changed(){

        },
        selected(){

        },
        trigger() {
           
              const url = [
                `${params.host}/citibot/${config.appContext}/app.html`,
                `?configurationId=${params.configurationId}`,
                `&botUserId=${params.botUserId}`,
                `&id=${config.appId}`,
              ];

              // invoke the module service to show our own application in the grid
              modulesService.show(
                config.appId,
                { title: config.appTitle },
                controllerName,
                url.join(''),
                { canFloat: true }
              );
        },
    });
}

/*
 * registerApplication                       register application on symphony client
 * @params       config                      app settings
 * @params       enrichers                   array of Enrichers to be registered in the application
 * @return       SYMPHONY.remote.hello       returns a SYMPHONY remote hello service.
 */
export const registerApplication = (config, appData, enrichers) => {
    const controllerName = `${config.appId}:controller`;

    let exportedDependencies = initEnrichers(enrichers);
    exportedDependencies.push(controllerName);

    const register = (data) => {
        registerEnrichers(enrichers);
        registerExtension(config);
        registerModule(config);

        return data;
    }

    return SYMPHONY.application.register(
        appData,
        dependencies,
        exportedDependencies
    ).then(register);
};
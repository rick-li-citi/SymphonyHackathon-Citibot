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
    'links',
    'share'
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
    const shareService = SYMPHONY.services.subscribe("share");

    let msgType = 'com.symphony.integration.zapier.event.v2.searchMessage';
    entityService.registerRenderer(msgType, {}, controllerName);


    const formatArticleUrl = article => {
        return article["content-type"] === "video/mp4" ? article.mediaUrlAndroid
             : article.documentType === "Commentary"? `https://uat.citivelocity.com/cv2/smartlink/commentary/${article.srcId}`
             : article.documentType === "Research"  ? `https://uat.citivelocity.com/cv2/smartlink/research/${article.srcId}`
             : "#";
    };

    let searchMsgType = 'com.symphony.integration.zapier.event.v2.searchMessage';
    let chartMsgType = 'com.symphony.integration.zapier.event.v2.chartMessage';
    entityService.registerRenderer(searchMsgType, {}, controllerName);
    entityService.registerRenderer(chartMsgType, {}, controllerName);
    const chartMap = {
        'ibm': 'CREDIT.BOND.US459200HK05.PRICE',
        'google': 'CREDIT.BOND.US38259PAB85.PRICE',
        'apple': 'CREDIT.BOND.US037833CF55.PRICE'
    }
    controllerService.implement({
        action: _.throttle(function(data){
            const article = data;
            shareService.share('article', {
                title: article.docTitle,
                subTitle: "",
                blurb: article.docTeaser,
                date : article.miliseconds / 1000,
                publisher: "Citi",
                author: article.analyst.join(','),
                href: formatArticleUrl(article)
            });            
        }, 300),

        render(type, data) {
            if (type === chartMsgType) {
                let ticker = data.data.ticker;
                ticker = ticker.replace('&nbsp;', '').trim();
                console.log('=== ', ticker);
              
                let chartUrl = _.escape('https://uat.citivelocity.com/analytics/charting3/?allowCross=false&chartTag='+chartMap[ticker]);
                console.log('chartUrl ', chartUrl);
                return {template: `<messageML>
                        <iframe src="${chartUrl}" height="300" width="400"/>
                    </messageML>`, data: {}};
            }
            if (type === searchMsgType) {
                
                let resultJson = JSON.parse(data.data).Results;

                const renderCategoryTitle = article => {
                    return article["content-type"] === "video/mp4" ? '<span class="tempo-text-color--green">[VIDEO]</span>'
                         : article.documentType === "Commentary"? '<span class="tempo-text-color--orange">[COMMENTARY]</span>'
                         : article.documentType === "Research"  ? '<span class="tempo-text-color--purple">[RESEARCH]</span>'
                         : "";
                };

                let actions = {};

                const addShareButton = article => {
                    actions[article.srcId] = {
                        label: ' ',
                        service: controllerName,
                        data: article,
                        icon: 'https://cdn3.iconfinder.com/data/icons/social-media-2-2/256/Share-16.png'
                    };
                    return `<action id="${article.srcId}" class="tempo-text-color--link"/>`
                }
                
                let resultML = resultJson.map( article => {
                    console.log(article);
                    return `
                    <card class="barStyle" accent-color="tempo-bg-color--green" icon-src="http://rick-li.ngrok.io/citibot/apps/citibot/img/bigicons_bigicon_doc.svg.png">
                        <header>
                            <div>
                                ${renderCategoryTitle(article)}
                                <a class="tempo-text-color--link" href="${formatArticleUrl(article)}">${_.escape(article.docTitle)}</a>
                                &nbsp; ${addShareButton(article)}
                            </div>
                        </header>
                        <body>
                        <div>
                            ${article.analyst ? `<span class="tempo-text-color--secondary">Author</span>
                            <span class="tempo-text-color--normal">${article.analyst.join(',')}</span>&nbsp; &nbsp;` : ''}                            
                            <span class="tempo-text-color--secondary">Date</span>
                            <span class="tempo-text-color--normal">${new Date(article.miliseconds).toString()}</span>
                        </div>
                        <div>
                            <br />
                            <span class="tempo-text-color--secondary">Description:</span>
                            <span class="tempo-text-color--normal">${_.escape(article.docTeaser)}</span>
                            <br/>
                            ${article.coverImageURL ? '<img src="${article.coverImageURL}"/>' : ''}
                        </div>
                        
                    <hr/>
                    </body>
                    </card>`
                });
                
                return {template: `<messageML>${resultML.join('')}</messageML>`, data: actions};
                // return {
                //     template: `
                //     <messageML>
                   
                //         <div>
                //             <a class="tempo-text-color--link" href="www.google.com">xxxx</a>
                //                 <span>Author</span>
                //                 <span class="tempo-text-color--blue">cccc</span> 
                //         </div>
                   
                //     <div>
                        
                //         <span class="tempo-text-color--secondary">Description:</span>
                        
                //         <span class="tempo-text-color--normal">dccc</span>

                //     <br/>
                    
                // </div>
                // <hr/>
                // <div>
                // <action id="assignTo" class="tempo-btn--primary"/>
                // </div>
                
                
                    
                //     </messageML>
                //     `,
                //     data: {assignTo: {label: 'Chart', service: controllerName, data: {a: 111}}}
                // }
                // return {
                //     // template: `<messageML>
                //     //     <div >hello world</div>
                //     //     <iframe height="200" width="400" src="https://uat.citivelocity.com/analytics/charting3/?allowCross=false" />
                        
                //     // </messageML>`,
                //     template: `
                //     <messageML>
                //     <div class="entity" >
                //     <card class="barStyle" accent="tempo-bg-color--green" iconSrc="https://cdn1.iconfinder.com/data/icons/logotypes/32/chrome-32.png">
                //         <header>
                //             <div>
                //                 <img src="http://rick-li.ngrok.io/citibot/apps/citibot/img/bigicons_bigicon_doc.svg.png" class="tempo-icon--document" />
                                
                //                 <a class="tempo-text-color--link" href="www.google.com">hello google</a>
                //                     <span class="tempo-text-color--normal">Subject is  - </span>
                //                     <span>User</span>
                //                     <span class="tempo-text-color--green">action</span>
                                
                //             </div>
                //         </header>
                //         <body>
                //             <div class="labelBackground badge">
                //                 <div>
                //                         <span class="tempo-text-color--secondary">Description:</span>
                //                         <span class="tempo-text-color--normal">xxxxxxxxx</span>
                //                     <br/>
                //                     <span class="tempo-text-color--secondary">Assignee:</span>
                //                         <mention email="racke1983cn@gmail.com" />
                //                 </div>
                //                 <hr/>
                //                 <div>
                //                     <div>
                //                     <img src="https://uat.citivelocity.com/analytics/eppublic/chartingbe/images/a413e76d-0069-432f-9265-e8d3520fb837.png"/>
                //                     </div>
                //                     <div>
                //                         <span class="tempo-text-color--secondary">&#160;&#160;&#160;Epic:</span>
                //                         <a href="http://google.com">google</a>
                //                     <span class="tempo-text-color--secondary">&#160;&#160;&#160;Status:</span>
                //                     <span class="tempo-bg-color--red tempo-text-color--white tempo-token">
                //                         testtesttest
                //                     </span>
            
            
                                    
                //                         <span class="tempo-text-color--secondary">&#160;&#160;&#160;Labels:</span>
                                        
                //                             <span class="hashTag">#ddd</span>
                //                         </div>
                                    
                //                 </div>
                //             </div>
                //         </body>
                //     </card>
                // </div>
                // </messageML>
                //     `,
                //     data: {}
                // };
               
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
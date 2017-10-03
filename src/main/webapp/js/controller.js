import 'babel-polyfill';
import { initUnauthenticatedApp } from './initApp';
import config from './config.service';
import _ from 'lodash';

initUnauthenticatedApp(config, [], () => {
    // SYMPHONY.services.make('citibotDialogService', {
    //     render: _.throttle(function(data){
    //         console.log('=== render data ', data);
    //         return `<iframe src="https://uat.citivelocity.com/analytics/charting3/?allowCross=false" width="500" height="400"/>`
    //     }, 300),
    //     // doneText(){
    //     //     return 'Done';
    //     // },
    //     done(){
    //         console.log('==== done ====')
    //     }
    // }, ['render', 'done'])
    
    // const dependencies = [
    //     'ui',
    //     'extended-user-info',
    //     'modules',
    //     'entity',
    //     'dialogs',
    //     'links'
    // ];
    // setTimeout(() => {
    //     SYMPHONY.application.connect('citibot', dependencies, ['citibotDialogService']);
    // }, 1000);
    
    
    let uiService = SYMPHONY.services.subscribe('ui');
    let cvControllerService = SYMPHONY.services.subscribe("citibot:controller");
    // The application service that will handle the filter on UI extensions
    let cvFilterService = SYMPHONY.services.register("cv:filter");
  

    uiService.registerExtension(
      'hashtag',
      'cv-assistant', 
      'citibot:controller', 
      {
        label: 'SearchCV',
        data: { 'datetime': Date() }
      }
    );
   
});







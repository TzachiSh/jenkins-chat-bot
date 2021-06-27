#!/usr/bin/env groovy

import static groovy.json.JsonOutput.toJson

void call(final Map<String, String> buildProperties = [:], final String url = env.GOOGLE_BOT_URL) {

    hook = registerWebhook()
    
    final Map<String, Object> complexMessage = [
                url: hook.getURL(),
                buildTag: "${env.BUILD_TAG}",
                inventory: "${inventory}",
                cause: "${currentBuild."buildCauses"?.shortDescription?.join(", ")}",
                console: "${env.BUILD_URL}console",
                sections: [],
                header: [
                    title: "${env.JOB_NAME}",
                    subtitle: "#${env.BUILD_NUMBER} Execute",
                    imageUrl: 'https://media3.giphy.com/media/131tNuGktpXGhy/giphy.gif?cid=ecf05e47igh0boaa1j9n5o3riazhb7dj703p7r8m522528f8&rid=giphy.gif',
                    imageStyle: 'AVATAR'
                ]
    ]
    

    
    
    if (buildProperties) {
        complexMessage.sections << [
            header: "Properties",
            widgets: buildProperties.collect { key, value ->
                [keyValue: [topLabel: "${key}", content: "${value}", contentMultiline: "true"]]
            }
        ]
    }

    try{
               def widgets = []
    if (currentBuild.changeSets.logs) {
        
            def changeLogSets = currentBuild.changeSets  
            for (int i = 0; i < changeLogSets.size(); i++) {
                def entries = changeLogSets[i].items
                for (int j = 0; j < entries.length; j++) {
                    def entry = entries[j]
                    widgets << [ 
                    keyValue: [
                              topLabel: "${new Date(entry.timestamp).toLocaleString()}",
                              content: "${entry.msg}",
                              bottomLabel: "${entry.author.displayName}",
                              contentMultiline: "true"]          
                    ]
                    def content = ""
                    def files = new ArrayList(entry.affectedFiles)
                    for (int k = 0; k < files.size(); k++) {
                        def file = files[k]
                        content += "${file.path} \n"
                        widgets << [
                                  keyValue:[
                                  topLabel:  "File paths",
                                  content: content,
                                  contentMultiline: "true"]
                        ]
                    }
                }
      }
      complexMessage.sections << [
            header: "<a href=\"${GIT_URL}/commits/${GIT_COMMIT}\">Changes</a>",
            widgets: widgets
        ]
    } 
    }catch(Exception e1) {
        //Catch block 
    }


    echo "Waiting for POST to ${hook.getURL()}"

    final String requestBody = toJson(complexMessage)
    echo requestBody
    httpRequest(requestBody: requestBody, url: url + '/jenkins?start=true', httpMode: 'POST', contentType: 'APPLICATION_JSON_UTF8')
    
    def data = waitForWebhook hook
    def webhookJson = readJSON text: data
    System.setProperty("webhookJson", data);
    echo "Webhook called with data: ${webhookJson['status']}"
    if (webhookJson['status'] == 400) {
    currentBuild.result = 'ABORTED'
    error('Stopping early…')
    }
    
}
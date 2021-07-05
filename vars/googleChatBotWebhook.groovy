#!/usr/bin/env groovy

import static groovy.json.JsonOutput.toJson

void call(final Map<String, String> buildProperties = [:],final Map<String, String> buildData = [:], final String url = env.GOOGLE_BOT_URL) {

    hook = registerWebhook()
    
    final Map<String, Object> complexMessage = [
                url: hook.getURL(),
                buildTag: "${env.BUILD_TAG}",
                stage: "${STAGE_NAME}"
    ]
    
    echo "Waiting for POST to ${hook.getURL()}"

    final String requestBody = toJson(complexMessage)
    echo requestBody
    httpRequest(requestBody: requestBody, url: url + '/jenkins?webhook=true', httpMode: 'POST', contentType: 'APPLICATION_JSON_UTF8')
    
    def data = waitForWebhook hook
    def webhookJson = readJSON text: data
    System.setProperty("webhookJson", data);
    echo "Webhook called with data: ${webhookJson['status']}"
    if (webhookJson['status'] == 400) {
    currentBuild.result = 'ABORTED'
    error('Stopping early…')
    }
    
}
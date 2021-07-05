#!/usr/bin/env groovy

import static groovy.json.JsonOutput.toJson

void call(final Map<String, String> buildProperties = [:], final String url = env.GOOGLE_BOT_URL) {
    final Map<String, String> RESULT_IMGS = [
        SUCCESS: "https://raw.githubusercontent.com/jenkinsci/modernstatus-plugin/master/src/main/webapp/48x48/blue.png",
        UNSTABLE: "https://jenkins.io/doc/book/resources/blueocean/dashboard/status-unstable.png",
        FAILURE: "https://raw.githubusercontent.com/jenkinsci/modernstatus-plugin/master/src/main/webapp/48x48/red.png",
        NOT_BUILT: "https://raw.githubusercontent.com/jenkinsci/modernstatus-plugin/master/src/main/webapp/48x48/yellow.png",
        ABORTED: "https://raw.githubusercontent.com/jenkinsci/modernstatus-plugin/master/src/main/webapp/48x48/aborted.png"
    ]
    final Map<String, String> RESULT_TEXT = [
        SUCCESS: "SUCCESS",
        UNSTABLE: "is unstable",
        FAILURE: "FAILED",
        NOT_BUILT: "is in progress",
        ABORTED: "is aborted"
    ]
    final Map<String, Object> complexMessage = [
        buildTag: "${env.BUILD_TAG}",
        sections: [],
        header: [
                    title: "${env.JOB_NAME}",
                    subtitle: "#${env.BUILD_NUMBER} ${RESULT_TEXT[currentBuild.currentResult]}",
                    imageUrl: RESULT_IMGS[currentBuild.currentResult] ?: RESULT_IMGS["NOT_BUILT"],
                    imageStyle: "AVATAR"
                ]
    ]

    if (buildProperties.message) {
        final String message = buildProperties.remove("message")
        complexMessage.sections << [
            "widgets": [
                [
                    textParagraph: [text: message]
                ]
            ]
        ]
    }

    if (buildProperties) {
        complexMessage.sections << [
            header: "Stage ${STAGE_NAME}",
            widgets: buildProperties.collect { key, value ->
                [keyValue: [topLabel: "${key}", content: "${value}", contentMultiline: "true"]]
            }
        ]
    }

    if (params) {
        complexMessage.sections << [
            header: "Parameters",
            widgets: params.collect { key, value ->
                [keyValue: [topLabel: "${key}", content: "${value}"]]
            }
        ]
    }
    
    final String requestBody = toJson(complexMessage)
    echo requestBody

    httpRequest(requestBody: requestBody, url: url + '/jenkins', httpMode: 'POST', contentType: 'APPLICATION_JSON_UTF8')
}
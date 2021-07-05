#!/usr/bin/env groovy

import static groovy.json.JsonOutput.toJson

void call(final Map<String, String> buildProperties = [:], final String url = env.GOOGLE_BOT_URL) {
    
    final Map<String, Object> complexMessage = [
        buildTag: "${env.BUILD_TAG}",
        sections: []
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
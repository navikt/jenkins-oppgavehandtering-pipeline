import groovy.json.JsonOutput

def call(Map args) {
    switch (args.action) {
        case 'validate': return validate()
        case 'upload': return upload()
        case 'jiraDeploy': return jiraDeploy()
        case 'jiraDeployMultiple': return jiraDeployMultiple()
        case 'jiraDeployProd': return jiraProdPost(args.jiraIssueId)
        case 'waitForCallback': return waitForCallback()
        default: error 'Nais-lib(jenkins) has been called with invalid argument'
    }
}

def validate() {
    sh "/usr/local/bin/nais validate"
}

def upload() {
    upload(env.APPLICATION_NAME, env.APPLICATION_VERSION)
}

def upload(final String applicationName, final String applicationVersion) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'Nexus', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD']]) {
        sh "/usr/local/bin/nais upload -a=${applicationName} -v=${applicationVersion}"
    }
}

def jiraDeploy() {
    return jiraDeploy(env.BUILD_URL, env.FASIT_ENV, env.NAMESPACE, env.APPLICATION_NAME, env.APPLICATION_VERSION)
}

def jiraDeploy(final String buildUrl, final String fasitEnv, final String nameSpace, final String applicationName, final String applicationVersion) {
    String callbackUrl = "${buildUrl}input/Deploy".toString()
    return jiraPost(callbackUrl, fasitEnv, nameSpace, applicationName, applicationVersion)
}

def jiraPost(final String callbackUrl) {
    return jiraPost(callbackUrl, env.FASIT_ENV, env.NAMESPACE, env.APPLICATION_NAME, env.APPLICATION_VERSION)
}

def jiraPost(final String callbackUrl, final String fasitEnv, final String nameSpace, final String applicationName, final String applicationVersion) {
    if (!fasitEnv) {
        error 'Environment variable FASIT_ENV must be specified'
    }
    if (!nameSpace) {
        error 'Environment variable NAMESPACE must be specified'
    }

    def postBody = [
            fields: [
                    project          : [key: "DEPLOY"],
                    issuetype        : [id: "14302"],
                    customfield_14811: [value: "${fasitEnv}"],
                    customfield_14812: "${applicationName}:${applicationVersion}",
                    customfield_17410: callbackUrl,
                    customfield_19015: [id: "22707", value: "Yes"],
                    customfield_19413: "${nameSpace}",
                    customfield_19610: [value: "fss"],
                    summary          : "Automatisk deploy av ${applicationName}:${applicationVersion} til ${fasitEnv} (${nameSpace} namespace)"
            ]
    ]
    
    return jiraPostRequest(postBody, fasitEnv)
}

def jiraProdPost(final String jiraIssueId) {
    def service = env.APPLICATION_SERVICE
    def component = env.APPLICATION_COMPONENT
    def name = env.APPLICATION_NAME
    def version = env.APPLICATION_VERSION
    def postBody = [
            fields: [
                    project          : [key: "PROD"],
                    issuetype        : [id: "15101"],
                    summary          : "${name}:${version}",
                    customfield_21440: ["id": "25705"],
                    customfield_20761: ["id": "24993"],
                    customfield_21110: ["id": "25279"]
            ],
            update: [
                    issuelinks       : [[
                                                add: [
                                                        type       : [
                                                                id: "10080"
                                                        ],
                                                        inwardIssue: [key: jiraIssueId]
                                                ]
                                        ]],
                    customfield_20768: [[set: [[key: service]]]],
                    customfield_20717: [[set: [[key: component]]]]
            ]
    ]
    return jiraPostRequest(postBody)
}

def jiraPostRequest(final postBody) {
    return jiraPostRequest(postBody, env.FASIT_ENV)
}

def jiraPostRequest(final postBody, final String fasitEnv) {
    def jiraPayload = JsonOutput.toJson(postBody)
    echo jiraPayload
    def response = httpRequest([
            url                   : "https://jira.adeo.no/rest/api/2/issue/",
            authentication        : "JIRA",
            consoleLogResponseBody: true,
            contentType           : "APPLICATION_JSON",
            httpMode              : "POST",
            requestBody           : jiraPayload])
    def jiraIssueId = readJSON([text: response.content])["key"].toString()
    def description = "${fasitEnv} - $jiraIssueId"
    currentBuild.description = description
    
    return jiraIssueId
}

def waitForCallback() {
    try {
        timeout(time: 1, unit: 'HOURS') {
            input id: "deploy", message: "Waiting for remote Jenkins server to deploy the application..."
        }
    } catch (Exception exception) {
        echo exception.getClass().getName()
        currentBuild.description = "Deploy failed, see " + currentBuild.description
        throw exception
    }
}

return this;

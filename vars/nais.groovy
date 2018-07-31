import groovy.json.JsonOutput

def call(Map args) {
    switch (args.action) {
        case 'validate': return validate()
        case 'upload': return upload()
        case 'jiraDeploy': return jiraDeploy()
        case 'jiraDeployMultiple': return jiraDeployMultiple()
        case 'jiraDeployProd' : return jiraProdPost(args.jiraIssueId)
        default: error 'nais has been called without valid arguments'
    }
}

def validate() {
    sh "nais validate"
}

def upload() {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus-user', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD']]) {
        sh "nais upload --app ${env.APPLICATION_NAME} -v ${env.APPLICATION_VERSION}"
    }
}

def jiraDeploy() {
    String url = "${env.BUILD_URL}input/Deploy".toString()
    return jiraPost(url)
}

def jiraDeployMultiple() {
    String url = "${env.BUILD_URL}input/${env.APPLICATION_NAME.capitalize()}-deploy/".toString()
    return jiraPost(url)
}

def jiraPost(String callbackUrl) {
    if(!env.FASIT_ENV) {
        error 'Environment variable FASIT_ENV must be specified'
    }
    if(!env.NAMESPACE) {
        error 'Environment variable NAMESPACE must be specified'
    }
    def name = env.APPLICATION_NAME
    def version = env.APPLICATION_VERSION
    def environment = env.FASIT_ENV
    def namespace = env.NAMESPACE

    def postBody = [
            fields: [
                    project          : [key: "DEPLOY"],
                    issuetype        : [id: "14302"],
                    customfield_14811: [value: "${environment}"],
                    customfield_14812: "${name}:${version}",
                    customfield_17410: callbackUrl,
                    customfield_19015: [id: "22707", value: "Yes"],
                    customfield_19413: "${namespace}",
                    customfield_19610: [value: "fss"],
                    summary          : "Automatisk deploy av ${name}:${version} til ${environment} (${namespace} namespace)"
            ]
    ]
    return jiraPostRequest(postBody)
}

def jiraProdPost(String jiraIssueId) {
    def service = env.APPLICATION_SERVICE
    def component = env.APPLICATION_COMPONENT
    def name = env.APPLICATION_NAME
    def version = env.APPLICATION_VERSION
    def postBody = [
            fields: [
                    project: [key: "PROD"],
                    issuetype: [id: "15101"],
                    summary: "${name}:${version}",
                    customfield_21440: ["id": "25705"],
                    customfield_20761: ["id": "24993"],
                    customfield_21110: ["id": "25279"]
            ],
            update: [
                    issuelinks: [[
                                         add: [
                                                 type: [
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

def jiraPostRequest(postBody) {
    def jiraPayload = JsonOutput.toJson(postBody)
    echo jiraPayload

    withCredentials([[$class: "UsernamePasswordMultiBinding", credentialsId: "JIRA", usernameVariable: "JIRA_USERNAME", passwordVariable: "JIRA_PASSWORD"]]) {
        def response = httpRequest([
                url                   : "https://jira.adeo.no/rest/api/2/issue/",
                authentication        : "nais-user",
                consoleLogResponseBody: true,
                contentType           : "APPLICATION_JSON",
                httpMode              : "POST",
                requestBody           : jiraPayload
        ])
        def jiraIssueId = readJSON([text: response.content])["key"].toString()
        def description = "${env.FASIT_ENV} - <a href=\"https://jira.adeo.no/browse/$jiraIssueId\">$jiraIssueId</a>"
        if (currentBuild.description?.trim()) {
            currentBuild.description += "<br> $description"
        } else {
            currentBuild.description = description
        }
        return jiraIssueId
    }
}
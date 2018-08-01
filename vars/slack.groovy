import net.sf.json.JSONArray
import net.sf.json.JSONObject

def call(Map args) {
    switch (args.status) {
        case 'started': return slackBuildStarted(args.changeLog)
        case 'passed': return slackBuildPassed()
        case 'deployed': return slackBuildDeployed(args.jiraIssueId)
        case 'aborted': return slackBuildAborted()
        case 'success': return slackBuildSuccess()
        case 'failure': return slackBuildFailed()
        default: error 'slackStatus has been called without valid arguments'
    }
}

def slackMessageAttachments(Map args) {
    def footer = "<${env.BUILD_URL}|${env.APPLICATION_NAME} - #${env.BUILD_NUMBER} - v${env.APPLICATION_VERSION}>".toString()

    JSONArray attachments = new JSONArray()
    JSONObject attachment = new JSONObject()

    attachment.put('title', args.title)
    attachment.put('title_link', args.get('titleLink', ''))
    attachment.put('text', args.get('text', ''))
    attachment.put('fallback', args.fallback)
    attachment.put('color', args.color)
    attachment.put('mrkdwn_in', ['text'])
    attachment.put('footer', footer)
    attachments.add(attachment)

    slackSend(color: '#00FF00', attachments: attachments.toString())
}

def slackBuildStarted(String changeLog) {
    Map vars = [:]
    vars.title = "Started on ${env.NODE_NAME} :hypers:".toString()
    vars.titleLink = "${env.BUILD_URL}".toString()
    vars.fallback = "Started: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#D4DADF"
    vars.text = changeLog
    return slackMessageAttachments(vars)
}

def slackBuildPassed() {
    Map vars = [:]
    vars.title = "Tests passed in ${currentBuild.durationString.replace(' and counting', '')} :feelsrareman:".toString()
    vars.fallback = "Tests passed: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#FFFE89"
    return slackMessageAttachments(vars)
}

def slackBuildDeployed(String jiraIssueId) {
    Map vars = [:]
    vars.title = "Deployed: ${jiraIssueId} to ${env.FASIT_ENV} :slow_parrot:".toString()
    vars.titleLink = "https://jira.adeo.no/browse/$jiraIssueId".toString()
    vars.fallback = "Deploying: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#FFFE89"
    return slackMessageAttachments(vars)
}

def slackBuildAborted() {
    Map vars = [:]
    vars.title = "Aborted :confused_parrot:"
    vars.fallback = "Aborted: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#FF9FA1"
    return slackMessageAttachments(vars)
}

def slackBuildSuccess() {
    Map vars = [:]
    vars.title = "Finished in ${currentBuild.durationString.replace(' and counting', '')} :ultrafast_parrot:".toString()
    vars.fallback = "Finished: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#BDFFC3"
    return slackMessageAttachments(vars)
}

def slackBuildFailed() {
    Map vars = [:]
    vars.title = "Failed :explody_parrot:"
    vars.fallback = "Failed: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#FF9FA1"
    return slackMessageAttachments(vars)
}
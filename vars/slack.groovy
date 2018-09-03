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
    def footer = "<${env.BUILD_URL}|${env.APPLICATION_NAME}:${env.APPLICATION_VERSION}  #${env.BUILD_NUMBER} (${env.CURRENT_STAGE})".toString()

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

def slackBuildDeployed(String jiraIssueId) {
    Map vars = [:]
    vars.title = "Deployed: ${env.APPLICATION_NAME}:${env.APPLICATION_VERSION} (${jiraIssueId}) to ${env.FASIT_ENV}".toString()
    vars.titleLink = "https://jira.adeo.no/browse/$jiraIssueId".toString()
    vars.fallback = "Deployed: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#FFFE89"
    return slackMessageAttachments(vars)
}

def slackBuildAborted() {
    Map vars = [:]
    vars.title = "Build aborted - ${env.CURRENT_STAGE}"
    vars.fallback = "Aborted ${env.CURRENT_STAGE}: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#FF9FA1"
    return slackMessageAttachments(vars)
}

def slackBuildSuccess() {
    Map vars = [:]
    vars.title = "Finished in ${currentBuild.durationString.replace(' and counting', '')}".toString()
    vars.fallback = "Finished: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#BDFFC3"
    return slackMessageAttachments(vars)
}

def slackBuildFailed() {
    Map vars = [:]
    vars.title = "Build failed - ${env.CURRENT_STAGE}"
    vars.fallback = "Failed ${env.CURRENT_STAGE}: #${env.BUILD_NUMBER} of ${env.APPLICATION_NAME} - ${env.BUILD_URL}".toString()
    vars.color = "#FF9FA1"
    return slackMessageAttachments(vars)
}
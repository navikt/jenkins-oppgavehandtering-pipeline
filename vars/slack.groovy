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
    return slackMessageAttachments(args, env.BUILD_URL, env.APPLICATION_NAME, env.APPLICATION_VERSION, env.BUILD_NUMBER, env.BRANCH_NAME)
}

def slackMessageAttachments(final Map args, final String buildUrl, final String applicationName, final String applicationVersion, final String buildNumber, final String branchName) {
    def footer = "${buildUrl}|${env.applicationName}:${env.applicationVersion} #${buildNumber} (${branchName})".toString()

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

def slackBuildDeployed(final String jiraIssueId) {
    return slackBuildDeployed(jiraIssueId, env.APPLICATION_NAME, env.APPLICATION_VERSION, env.FASIT_ENV, env.BUILD_NUMBER, env.BUILD_URL, env.BRANCH_NAME)
}

def slackBuildDeployed(final String jiraIssueId, final String applicationName, final String applicationVersion, final String fasitEnv, final String buildNumber, final String buildUrl, final String branchName) {
    Map vars = [:]
    vars.title = "Deployed: ${applicationName}:${applicationVersion} (${jiraIssueId}) to ${fasitEnv}".toString()
    vars.titleLink = "https://jira.adeo.no/browse/$jiraIssueId".toString()
    vars.fallback = "Deployed: #${buildNumber} of ${applicationName} - ${buildUrl}".toString()
    vars.color = "#FFFE89"

    return slackMessageAttachments(vars, buildUrl, applicationName, applicationVersion, buildNumber, branchName)
}

def slackBuildAborted() {
    return slackBuildAborted(env.CURRENT_STAGE, env.BUILD_NUMBER, env.APPLICATION_NAME, env.APPLICATION_VERSION, env.BUILD_URL, env.BRANCH_NAME);
}

def slackBuildAborted(final String currentStage, final String buildNumber, final String applicationName, final String applicationVersion, final String buildUrl, final String branchName) {
    Map vars = [:]
    vars.title = "Build aborted (stage: ${currentStage})".toString()
    vars.fallback = "Aborted ${currentStage}: #${buildNumber} of ${applicationName} - ${buildUrl}".toString()
    vars.color = "#FF9FA1"
    
    return slackMessageAttachments(vars, buildUrl, applicationName, applicationVersion, buildNumber, branchName)
}

def slackBuildSuccess() {
    return slackBuildSuccess(env.CURRENT_STAGE, env.BUILD_NUMBER, env.APPLICATION_NAME, env.APPLICATION_VERSION, env.BUILD_URL, env.BRANCH_NAME)
}

def slackBuildSuccess(final String currentStage, final String buildNumber, final String applicationName, final String applicationVersion, final String buildUrl, final String branchName) {
    Map vars = [:]
    vars.title = "Finished in ${currentBuild.durationString.replace(' and counting', '')}".toString()
    vars.fallback = "Finished: #${buildUrl} of ${applicationName} - ${buildUrl}".toString()
    vars.color = "#BDFFC3"
    
    return slackMessageAttachments(vars, buildUrl, applicationName, applicationVersion, buildNumber, branchName)
}

def slackBuildFailed() {
    return slackBuildFailed(env.CURRENT_STAGE, env.BUILD_NUMBER, env.APPLICATION_NAME, env.APPLICATION_VERSION, env.BUILD_URL, env.BRANCH_NAME)
}

def slackBuildFailed(final String currentStage, final String buildNumber, final String applicationName, final String applicationVersion, final String buildUrl, final String branchName) {
    Map vars = [:]
    vars.title = "Build failed (stage: ${env.CURRENT_STAGE})".toString()
    vars.fallback = "Failed ${currentStage}: #${buildNumber} of ${applicationName} - ${buildUrl}".toString()
    vars.color = "#FF9FA1"

    return slackMessageAttachments(vars, buildUrl, applicationName, applicationVersion, buildNumber, branchName)
}

return this;

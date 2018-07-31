def call() {
    if(!env.APPLICATION_NAME) {
        error 'environment variable: APPLICATION_NAME is required'
    } else if(!env.APPLICATION_VERSION) {
        error 'environment variable: APPLICATION_VERSION is required'
    }

    withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
        withCredentials([string(credentialsId: 'github-token', variable: 'github-token')]) {
            sh ("git tag -a ${env.APPLICATION_VERSION} -m ${env.APPLICATION_VERSION}")
            sh ("git tag push https://${github-token}:x-oauth-basic@github.com/navikt/${env.APPLICATION_NAME}.tag --tags")
        }
    }
}

def call() {
    if(!${env.APPLICATION_VERSION} || !${env.APPLICATION_NAME}) {
        error 'environment variables APPLICATION_NAME and APPLICATION_VERSION are required'
    }

    withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
        withCredentials([string(credentialsId: 'github-token', variable: 'github-token')]) {
            sh ("tag tag -a ${env.APPLICATION_VERSION} -m ${env.APPLICATION_VERSION}")
            sh ("tag push https://${github-token}:x-oauth-basic@github.com/navikt/${env.APPLICATION_NAME}.tag --tags")
        }
    }
}

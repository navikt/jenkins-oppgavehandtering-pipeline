def call(final String action) {
    switch (action) {
        case 'buildAndPush': return buildAndPush()
        case 'prune': return prune()
        default: error 'docker has been called with invalid/missing arguments'
    }
}

def buildAndPush() {
    buildAndPush(env.APPLICATION_NAME, env.APPLICATION_VERSION)
}

def buildAndPush(final String  applicationName, final String applicationVersion) {
    def image = docker.build("docker.adeo.no:5000/" + applicationName + ":" + applicationVersion, "--pull .")
    image.push()
}

def prune() {
    sh "docker image prune -f"
}

return this;

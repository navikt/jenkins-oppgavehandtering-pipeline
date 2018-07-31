def call(String action) {
    switch (action) {
        case 'buildAndPush': return buildAndPush()
        case 'prune': return prune()
        default: error 'docker has been called with invalid/missing arguments'
    }
}

def buildAndPush() {
    def image = docker.build("docker.adeo.no:5000/${env.APPLICATION_NAME}:${env.APPLICATION_VERSION}", "--pull .")
    image.push()
}

def prune() {
    sh "docker image prune -f"
}

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
    
    def fullDockerImageName = applicationName + ":" + applicationVersion
    def repoUrl = "https://repo.adeo.no:5443/"
	docker.withRegistry("${repoUrl}", "JENKINS_NEXUS_REPO_ADEO_USER") {
		echo "About to build docker image..."
		def image = docker.build("${repoUrl}" + "${fullDockerImageName}", "--pull .")
		echo "About to push the just built docker image..."
		image.push()
		echo "The docker image successfully built and pushed."
	}
    
    /*
    def image = docker.build("docker.adeo.no:5000/" + applicationName + ":" + applicationVersion, "--pull .")
    image.push()
    */
}

def prune() {
    sh "docker image prune -f"
}

return this;

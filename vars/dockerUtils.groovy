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
    
    def repoDomainAndPort = "repo.adeo.no:5443/"
    def fullDockerImageName = applicationName + ":" + applicationVersion
    def dockerTag = repoDomainAndPort + fullDockerImageName
    def repoProtocol = "https://"
    
    docker.withRegistry(repoProtocol + repoDomainAndPort, "JENKINS_NEXUS_REPO_ADEO_USER") {
        echo "About to build docker image tagged ${dockerTag}..."
	def image = docker.build(dockerTag, "--pull .")
	echo "About to push the just built docker image tagged ${dockerTag} ..."
	image.push()
	echo "The docker image successfully built and pushed."
    }
}

def prune() {
    sh "docker image prune -f"
}

return this;

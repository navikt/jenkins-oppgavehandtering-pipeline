def call() {
    dependencyCheckAnalyzer datadir: 'dependency-check-data',
            isFailOnErrorDisabled: true,
            hintsFile: '',
            includeCsvReports: false,
            includeHtmlReports: false,
            includeJsonReports: false,
            isAutoupdateDisabled: false,
            outdir: '',
            scanpath: '',
            skipOnScmChange: false,
            skipOnUpstreamChange: false,
            suppressionFile: 'owasp-suppression.xml',
            zipExtensions: ''

    dependencyCheckPublisher canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '', unHealthy: ''
    archiveArtifacts allowEmptyArchive: true, artifacts: '**/dependency-check-report.xml', onlyIfSuccessful: true
}
pipeline {
    agent any
    stages {
        stage ('Build') {
            steps {
                sh 'mvn clean install' 
                archiveArtifacts(artifacts: 'target/SynesthesiaGDPR-*.jar', allowEmptyArchive: true, fingerprint: true, onlyIfSuccessful: true)
            }
        }
    }
}

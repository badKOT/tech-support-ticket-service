pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        IMAGE_NAME = 'tech-support-backend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm

                script {
                    env.GIT_SHA = sh(
                        script: 'git rev-parse --short=12 HEAD',
                        returnStdout: true
                    ).trim()
                }

                sh 'git log -1 --oneline'
                echo "Image: ${env.IMAGE_NAME}:${env.GIT_SHA}"
            }
        }

        stage('Test and build') {
            steps {
                sh './gradlew clean build --no-daemon --max-workers=2'
            }

            post {
                always {
                    junit(
                        testResults: 'build/test-results/test/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('Build Docker image') {
            steps {
                sh '''
                    set -eu
                    docker build -t "${IMAGE_NAME}:${GIT_SHA}" .
                    docker image inspect "${IMAGE_NAME}:${GIT_SHA}" \
                        --format 'Tags={{json .RepoTags}} ID={{.Id}}'
                '''
            }
        }
    }
}
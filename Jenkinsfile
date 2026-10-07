pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        IMAGE_NAME = 'tech-support-backend'
        REGISTRY_IMAGE = 'ghcr.io/dorablebetscha/tech-support-backend'
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

        stage('Push Docker image') {
                    steps {
                        withCredentials([
                            usernamePassword(
                                credentialsId: 'ghcr-push',
                                usernameVariable: 'GHCR_USER',
                                passwordVariable: 'GHCR_TOKEN'
                            )
                        ]) {
                            sh '''
                                set +x
                                set -eu

                                DOCKER_CONFIG="$(mktemp -d)"
                                export DOCKER_CONFIG
                                trap 'rm -rf "$DOCKER_CONFIG"' EXIT

                                printf '%s' "$GHCR_TOKEN" | docker login ghcr.io \
                                    --username "$GHCR_USER" \
                                    --password-stdin

                                docker tag \
                                    "${IMAGE_NAME}:${GIT_SHA}" \
                                    "${REGISTRY_IMAGE}:${GIT_SHA}"

                                docker push "${REGISTRY_IMAGE}:${GIT_SHA}"
                            '''
                        }
                    }
        }

        stage('Deploy backend') {
            steps {
                sshagent(credentials: ['k3s-deploy-ssh']) {
                    sh '''
                        set -eu

                        sh -n scripts/deploy-backend-registry.sh

                        ssh \
                            -o BatchMode=yes \
                            -o StrictHostKeyChecking=yes \
                            -o ConnectTimeout=15 \
                            ticketuser@88.218.67.241 \
                            "sh -s -- '${REGISTRY_IMAGE}:${GIT_SHA}'" \
                            < scripts/deploy-backend-registry.sh
                    '''
                }
            }
        }
    }
}
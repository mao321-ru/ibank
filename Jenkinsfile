pipeline {
    agent any

    environment {
        PROJECT_NAME = 'ibank'
        ENV_NAME = "${GIT_BRANCH}"
        IMAGE_TAG = "jenkins-${ENV_NAME}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        stage('Init') {
            steps {
                echo "Run pipeline for project: ${PROJECT_NAME}"
                echo "BUILD_TAG: ${BUILD_TAG}"
                echo "BUILD_NUMBER: ${BUILD_NUMBER}"
                echo "JOB_NAME: ${JOB_NAME}"
                echo "GIT_BRANCH: ${GIT_BRANCH}"
                echo "GIT_COMMIT: ${GIT_COMMIT}"
                echo "ENV_NAME: ${ENV_NAME}"
                echo "IMAGE_TAG: ${IMAGE_TAG}"
                //sh 'printenv | sort'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Check tests') {
            steps {
                sh 'mvn verify'
            }
        }

        stage('Build docker image') {
            steps {
                sh 'TAG=${IMAGE_TAG} docker compose build'
            }
        }

        stage('Helm dependency update') {
            steps {
                sh 'helm dependency update ./chart'
            }
        }

        stage('Deploy') {
            steps {
                sh """
                helm upgrade --install ibank ./chart \\
                  --namespace ${ENV_NAME} --create-namespace \\
                  --set global.image.tag=${IMAGE_TAG} \\
                  --set global.domain=${ENV_NAME}.local \\

                """
            }
        }

    }

    post {
        success {
            echo "Finished OK"
        }
        failure {
            echo "Finished with ERROR"
        }
    }
}

pipeline {
  agent any

  environment {
    REPO_URL = 'https://github.com/gupta-gyan/hello-microservice.git'
    IMAGE_REPO = 'ankurgupta30/hello'
    DOCKER_CREDENTIALS_ID = 'dockerhub'
    GCP_CREDENTIALS_ID = 'gcp-key'
    PROJECT_ID = "river-formula-468310-b4"
    CLUSTER_NAME = 'hello-cluster'
    CLUSTER_ZONE = 'us-central1-c'
  }

  stages {
    stage('Clone') {
      steps {
        git REPO_URL
      }
    }

    stage('Build JAR') {
      steps {
        sh 'mvn clean package -DskipTests'
      }
    }
stage('Build & Push Image') {
  steps {
    script {
      // unique tag for this build
          def tag = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          env.IMAGE_TAG = tag
          env.IMAGE_REPO = 'ankurgupta30/hello'

          docker.withRegistry('', 'dockerhub') {
            def img = docker.build("${env.IMAGE_REPO}:${env.IMAGE_TAG}")
            img.push()  // push SHA tag

            // optional: also push :latest
            sh "docker tag ${env.IMAGE_REPO}:${env.IMAGE_TAG} ${env.IMAGE_REPO}:latest"
            sh "docker push ${env.IMAGE_REPO}:latest"
          }

        }
      }
    }

    stage('Deploy to GKE via Helm') {
      steps {
        withCredentials([file(credentialsId: 'gcp-key', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
          sh '''
            gcloud auth activate-service-account --key-file="$GOOGLE_APPLICATION_CREDENTIALS"
            gcloud config set project "$PROJECT_ID"
            gcloud container clusters get-credentials "$CLUSTER_NAME" --zone "$CLUSTER_ZONE"

            helm upgrade --install hello ./helm/hello \
              --set image.repository='${IMAGE_REPO}' \
              --set image.tag='${IMAGE_TAG}'
          '''
        }
      }
    }

  }
}


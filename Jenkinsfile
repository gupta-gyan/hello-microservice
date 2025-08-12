pipeline {
  agent any

  environment {
    IMAGE_NAME = "ankurgupta30/hello:latest"
    DOCKER_CREDENTIALS_ID = "dockerhub"
    GCP_CREDENTIALS_ID = "gcp-key"
    PROJECT_ID = "river-formula-468310-b4"
    CLUSTER_NAME = "hello-cluster"
    CLUSTER_ZONE = "us-central1-c"
  }

  stages {
    stage('Clone') {
      steps {
        git 'https://github.com/gupta-gyan/hello-microservice.git'
      }
    }

    stage('Build JAR') {
      steps {
        sh 'mvn clean package -DskipTests'
      }
    }

    stage('Build & Push Docker Image') {
      steps {
        script {
          docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
            def app = docker.build(IMAGE_NAME)
            app.push()
          }
        }
      }
    }

    stage('Deploy to GKE via Helm') {
      steps {
        withCredentials([file(credentialsId: GCP_CREDENTIALS_ID, variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
          sh '''
            gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
            gcloud config set project $PROJECT_ID
            gcloud container clusters get-credentials $CLUSTER_NAME --zone $CLUSTER_ZONE
            helm upgrade --install hello ./helm/hello --set image.repository=ankurgupta30/hello,image.tag=latest
          '''
        }
      }
    }
  }
}

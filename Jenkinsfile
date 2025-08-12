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
          // use short commit for tag
          def tag = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          env.IMAGE_TAG = tag

          docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
            def img = docker.build("${IMAGE_REPO}:${tag}")
            img.push()
            // optionally push :latest for humans
            sh "docker tag ${IMAGE_REPO}:${tag} ${IMAGE_REPO}:latest"
            sh "docker push ${IMAGE_REPO}:latest"
          }
        }
      }
    }

    stage('Deploy to GKE via Helm') {
      steps {
        withCredentials([file(credentialsId: GCP_CREDENTIALS_ID, variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
          sh '''
            gcloud auth activate-service-account --key-file="$GOOGLE_APPLICATION_CREDENTIALS"
            gcloud config set project "$PROJECT_ID"
            gcloud container clusters get-credentials "$CLUSTER_NAME" --zone "$CLUSTER_ZONE"

            # Deploy with specific tag (overrides values.yaml)
            helm upgrade --install hello ./helm/hello \
              --set image.repository='${IMAGE_REPO}' \
              --set image.tag='${IMAGE_TAG}'

            # Block until rollout finishes
            kubectl rollout status deploy/hello --timeout=120s

            # Show service endpoint
            kubectl get svc hello -o wide
          '''
        }
      }
    }
  }
}


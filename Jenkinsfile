pipeline {
  agent any

  environment {
    IMAGE_REPO   = 'ankurgupta30/hello'
    DOCKER_SECRET = 'dockerhub'
    GCP_CREDENTIALS_ID = 'gcp-key'
    PROJECT_ID   = 'river-formula-468310-b4'
    CLUSTER_NAME = 'hello-cluster'
    CLUSTER_ZONE = 'us-central1-c'
  }

  stages {
    stage('Build JAR') {
      agent {
        kubernetes {
          yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: maven
      image: maven:3.9.4-eclipse-temurin-17
      command: ['cat']
      tty: true
      volumeMounts:
        - name: m2
          mountPath: /root/.m2
  volumes:
    - name: m2
      emptyDir: {}
"""
          defaultContainer 'maven'
        }
      }
      steps {
        sh 'mvn -v'
        sh 'mvn clean package -DskipTests'
      }
    }

    stage('Build & Push Image (Kaniko)') {
      agent {
        kubernetes {
          yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: kaniko
      image: gcr.io/kaniko-project/executor:v1.23.2
      command: ['cat']
      tty: true
      volumeMounts:
        - name: docker-config
          mountPath: /kaniko/.docker
  volumes:
    - name: docker-config
      secret:
        secretName: ${DOCKER_SECRET}
        items:
          - key: .dockerconfigjson
            path: config.json
"""
          defaultContainer 'kaniko'
        }
      }
      steps {
        script {
          def tag = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          env.IMAGE_TAG = tag
        }
        sh '''
          /kaniko/executor \
            --context `pwd` \
            --dockerfile Dockerfile \
            --destination ${IMAGE_REPO}:${IMAGE_TAG} \
            --destination ${IMAGE_REPO}:latest
        '''
      }
    }

    stage('Deploy to GKE via Helm') {
      steps {
        withCredentials([file(credentialsId: GCP_CREDENTIALS_ID, variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
          sh '''
            gcloud auth activate-service-account --key-file="$GOOGLE_APPLICATION_CREDENTIALS"
            gcloud config set project "$PROJECT_ID"
            gcloud container clusters get-credentials "$CLUSTER_NAME" --zone "$CLUSTER_ZONE"
            helm upgrade --install hello ./helm/hello \
              --set image.repository="${IMAGE_REPO}" \
              --set image.tag="${IMAGE_TAG}"
          '''
        }
      }
    }
  }
}

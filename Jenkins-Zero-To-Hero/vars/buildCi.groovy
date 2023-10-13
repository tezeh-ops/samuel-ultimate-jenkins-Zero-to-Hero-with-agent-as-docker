
def call() {
    pipeline {
  agent {                    //we are using  docker container here as our Agent ( and the image in it contain of Maven and Docker as i explain above
    docker {
      image 'abhishekf5/maven-abhishek-docker-agent:v1'               // here is the image we are using
      args '--user root -v /var/run/docker.sock:/var/run/docker.sock'   // mount Docker socket to access the host's Docker daemon
    }
  }
  stages {     // this stage is not actuall need here since we CALLING our SRC from jenkins UI but if we con have writen this code in our Jenkins UI the chekout stage is needee
    stage('Checkout') {
      steps {
        sh 'echo passed'
        //git branch: 'main', url: 'https://github.com/tezeh-ops/samuel-ultimate-jenkins-Zero-to-Hero-with-agent-as-docker'   // that is why we comment this part
      }
    }
    stage('Build and Test') {
      steps {
        
        // build the project and create a JAR file And we are Cd here because we are working on the spring-boot-app so we hv to go there so as to build the artifacts that we need and the where our Pom.xml file is found since we need it to build our packages by making use of the indpendencies
        sh 'cd Jenkins-Zero-To-Hero/java-maven-sonar-argocd-helm-k8s/spring-boot-app && mvn clean package'
      }
    }
    stage('Static Code Analysis') {    // here is where sonarqube comes in
      environment {
        SONAR_URL = "http://34.235.135.118:9000"   // here just hard code the sonarqube URL so so that jenking we send the result to sonarqube
      }
      steps {
        withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
          sh 'cd Jenkins-Zero-To-Hero/java-maven-sonar-argocd-helm-k8s/spring-boot-app && mvn sonar:sonar -Dsonar.login=$SONAR_AUTH_TOKEN -Dsonar.host.url=${SONAR_URL}'
        }        // the line above we cd to were the applications file are found and we are using the credentials( token ) that we set up from sonarqube and the Sonsrqube URL
      }           // to connect to it through jenkins
    }
    stage('Build and Push Docker Image') {    // here we are build the image and pushing it  to our Docker registory
      environment {
        DOCKER_IMAGE = "tezeh/ultimate-cicd:${BUILD_NUMBER}"   
        // DOCKERFILE_LOCATION = "Jenkins-Zero-To-Hero/java-maven-sonar-argocd-helm-k8s/spring-boot-app/Dockerfile"
        REGISTRY_CREDENTIALS = credentials('docker-cred')  // here we are making use of the Docker credentials we have configure in jenkins manage credentials and we call it
      }                                                      //  < docker-cred>
      steps {
        script {
            sh 'cd Jenkins-Zero-To-Hero/java-maven-sonar-argocd-helm-k8s/spring-boot-app && docker build -t ${DOCKER_IMAGE} .'
            def dockerImage = docker.image("${DOCKER_IMAGE}")
            docker.withRegistry('https://index.docker.io/v1/', "docker-cred") {
                dockerImage.push()           // we are pushing the image to the registry
            }
        }
      }
    }
    stage('Update Deployment File') {     
        environment {                               // just setting some Environment variables to use and push our updated repo back to GitHub so we can use it in Argo CD
            GIT_REPO_NAME = "samuel-ultimate-jenkins-Zero-to-Hero-with-agent-as-docker"    // by pulling it from Argo CD automatically
            GIT_USER_NAME = "tezeh-ops"
        }
        steps {
            withCredentials([string(credentialsId: 'github', variable: 'GITHUB_TOKEN')]) {    // making use of our GitHub Credentials so that we will be able to push
                sh '''
                    git config user.email "samuelmunoh@gamil.com"
                    git config user.name "tezeh-ops"
                    BUILD_NUMBER=${BUILD_NUMBER}
                    sed -i "s/replaceImageTag/${BUILD_NUMBER}/g" Jenkins-Zero-To-Hero/java-maven-sonar-argocd-helm-k8s/spring-boot-app-manifests/deployment.yml
                    git add Jenkins-Zero-To-Hero/java-maven-sonar-argocd-helm-k8s/spring-boot-app-manifests/deployment.yml         
                    git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                    git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                '''
            }
        }
    }
  }
}
}
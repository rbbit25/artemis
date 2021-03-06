def k8slabel = "jenkins-pipeline-${UUID.randomUUID().toString()}"
def slavePodTemplate = """
      metadata:
        labels:
          k8s-label: ${k8slabel}
        annotations:
          jenkinsjoblabel: ${env.JOB_NAME}-${env.BUILD_NUMBER}
      spec:
        affinity:
          podAntiAffinity:
            requiredDuringSchedulingIgnoredDuringExecution:
            - labelSelector:
                matchExpressions:
                - key: component
                  operator: In
                  values:
                  - jenkins-jenkins-master
              topologyKey: "kubernetes.io/hostname"
        containers:
        - name: docker
          image: docker:latest
          imagePullPolicy: IfNotPresent
          command:
          - cat
          tty: true
          volumeMounts:
            - mountPath: /var/run/docker.sock
              name: docker-sock
        serviceAccountName: default
        securityContext:
          runAsUser: 0
          fsGroup: 0
        volumes:
          - name: docker-sock
            hostPath:
              path: /var/run/docker.sock
    """
    def environment = ""
    def branch = "${scm.branches[0].name}".replaceAll(/^\*\//, '').replace("/", "-").toLowerCase()
    def docker_image = ""

    docker_image = "rbbit/artemis:${branch.replace('version/', 'v')}"
      // master -> prod  dev-feature/* -> dev qa-feature/* -> qa
        if (branch == "master") {
        environment ="prod"
      } else if (branch.contains('dev-feature')) {
        environment = "dev" 
      } else if (branch.contains('qa-feature')) {
        environment = "qa" 
      }
      println("${environment}")

    
    podTemplate(name: k8slabel, label: k8slabel, yaml: slavePodTemplate, showRawYaml: false) {
      node(k8slabel) {
        stage('Pull SCM') {
            checkout scm
        }

        container("docker") {
            dir('deployments/docker') {
                stage("Docker Build") {
                    sh "docker build -t ${docker_image}  ."
                
                }
                stage("Docker Login") {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', passwordVariable: 'password', usernameVariable: 'username')]) {
                        sh "docker login --username ${username} --password ${password}"
                        
                    }
                }
                stage("Docker Push") {
                    sh "docker push ${docker_image}"
                    
                }

                stage("Trigger Deploy") {
                  build job: 'artemis-deploy',
                  parameters: [
                      [$class: 'BooleanParameterValue', name: 'terraformApply', value: true],
                      [$class: 'StringParameterValue',  name: 'environment', value: "${environment}"],
                      [$class: 'StringParameterValue',  name: 'docker_image', value: "${docker_image}"]
                      ]

                }
          }
        }

      }
    }
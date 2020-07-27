terraform {
    backend "s3" {
        bucket = "jenkins-instance-ec"
        key = "artemis/us-east-1/class/dev/infrastructure.tfstate"
        region = "us-east-1"
    }
}

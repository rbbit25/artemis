module "artemis-deploy" {
  source  = "fuchicorp/chart/helm"

  deployment_name        = "artemis"
  deployment_environment = "${var.environment}"
  deployment_endpoint    = "none" 
  deployment_path        = "artemis"

  template_custom_vars  = {     
    deployment_image     = "${var.deployment_image}"    
  }
}

resource "aws_rds_cluster_instance" "cluster_instances" {
  count              = 2
  identifier         = "aurora-cluster-demo-${count.index}"
  cluster_identifier = "${aws_rds_cluster.default.id}"
  instance_class     = "db.m1.small"
  engine             = "${aws_rds_cluster.default.engine}"
  engine_version     = "${aws_rds_cluster.default.engine_version}"
}

resource "aws_rds_cluster" "default" {
  cluster_identifier = "aurora-cluster-demo"
  availability_zones = ["us-west-2a", "us-west-2b", "us-west-2c"]
  database_name      = "mydb"
  master_username    = "foo"
  master_password    = "barbut8chars"
}


variable "environment" {
}


variable "deployment_image" {
  default = "fsadykov/artemis:master"
}


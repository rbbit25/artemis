properties([parameters([
        booleanParam(defaultValue: false, description: 'Please select to apply the changes', name: 'terraformApply'), 
        booleanParam(defaultValue: false, description: 'Please select to destroy everything', name: 'terraformDestroy'), 
        booleanParam(defaultValue: false, description: 'Please select to run the job in debug mode', name: 'debugMode'), 
        choice(choices: ['dev', 'qa', 'stage', 'prod'], description: 'Please select the environment to deploy.', name: 'environment'),
        string(defaultValue: 'None', description: 'Please provide the docker image', name: 'docker_image', trim: true)
        ])
    ])


println("""
##########################################
Terraform apply: ${params.terraformApply}
Selected env: ${params.environment}
##########################################
""")
println('Hello World')

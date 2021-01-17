def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
        
		   
	def PULL_REQUEST = env.CHANGE_ID
    def  ChangeCount = 1

  pipeline {
    agent any
    parameters {
		string defaultValue: '1', description: 'PULL_REQUEST number', name: 'PULL_REQUEST', trim: true
		string defaultValue: 'RELEASE-VARYS', description: 'Branch Name', name: 'Branch', trim: true
		}
		environment{
        PULL_REQUEST="${params.PULL_REQUEST}"
		}
		stages { 
              stage('checkOut') {
            steps {
			script {
				CURRENT_STAGE=env.STAGE_NAME
				echo "${CURRENT_STAGE}"
				}
                checkout([$class: 'GitSCM', branches: [[name: pipelineParams.Branch]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: pipelineParams.Credentials, url: pipelineParams.ScmUrl]]])
            	script{
              /* if(env.PULL_REQUEST == "1")
					{
						ChangeCount = "1"
					}
					else{
						ChangeCount = getChangeCount()
					} */
               // ChangeCount = getChangeCount()
               // echo ChangeCount
			   echo "checkout done"
                }
			
			}
        }
		 stage('Verify toJS') {
			when {
                // Executed only if change count is not 0
                expression { ChangeCount != "0" }
            }
				steps {
				script {
				try {
					CURRENT_STAGE=env.STAGE_NAME
				    sh "grep -r toJS * | grep -v node_modules | grep -v toJSON"
				    currentBuild.result = 'FAILURE'
				 
				 }
			catch(Exception e){
			}
			finally{
				if(currentBuild.result == 'FAILURE')
					throw new Exception("TO JS Found in above line")
			}
			
				}
			}
        }
		stage('Yarn Install') {
			when {
                // Executed only if change count is not 0
                expression { ChangeCount != "0" }
            }
            steps {
			script {
				CURRENT_STAGE=env.STAGE_NAME
				}
                sh """
				 echo create artifact here
				 export PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true
			     yarn 
				"""
			
			}
				
        }
    }
       
  }

}

    
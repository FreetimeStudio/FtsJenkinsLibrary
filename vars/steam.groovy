//env.STEAM_SDK_PATH

def writeDepotVDF(String depotId) {        
        writeFile file: "${STEAM_SDK_PATH}/tools/ContentBuilder/scripts/depot_${depotId}.vdf", 
                    text: """"DepotBuildConfig"
                            {
                                "DepotID" "${depotId}"
                                "contentroot" "${BUILD_OUTPUT_PATH}"
                                "FileMapping"
                                {
                                    "LocalPath" "*"
                                    "DepotPath" "."
                                    "recursive" "1"
                                }
                                "FileExclusion" "*.pdb"
                            }""".stripIndent()
}

def writeAppVDF(String appId, String depotId, String buildComment) {
        writeFile file: "${STEAM_SDK_PATH}/tools/ContentBuilder/scripts/app_${appId}.vdf", 
                    text: """"appbuild"
                            {
                                "appid" "${appId}"
                                "desc" "${env.BUILD_VERSION}        ${buildComment}"
                                "buildoutput" "${STEAM_SDK_PATH}/tools/ContentBuilder/output"
                                "contentroot" ""
                                "setlive" "develop"
                                "preview" "0"
                                "local"	""
                                "depots"
                                {
                                    "${depotId}"	"${STEAM_SDK_PATH}/tools/ContentBuilder/scripts/depot_${depotId}.vdf"
                                }
                            }""".stripIndent()
}

def upload(String appId, String credentialsId) {
    withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'steamPass', usernameVariable: 'steamUser')]) {
       bat(script: "\"${STEAM_SDK_PATH}/tools/ContentBuilder/builder/steamcmd.exe\" \"+login\" \"${steamUser}\" \"${steamPass}\" \"+run_app_build\" \"${STEAM_SDK_PATH}/tools/ContentBuilder/scripts/app_${appId}.vdf\" \"+quit\"")
   }
}

def deploy(Map config = [:])
{
/*
    def defaultConfig = [
        appId: '',
        depotId: '',
        credentialsId: '',
        buildComment: ''
    ]
*/
    writeDepotVDF(config.depotId)
    writeAppVDF(config.appId, config.depotId, config.buildComment)
    upload(config.appId, config.credentialsId)
}
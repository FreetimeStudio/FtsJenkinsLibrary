import net.freetimestudio.Platform

//Needs env.STEAM_SDK_PATH

def getSteamBuilderPath()
{
    if(isUnix()) {
        return "${STEAM_SDK_PATH}/tools/ContentBuilder/builder_osx/steamcmd.sh" 
    }
    
    return "${STEAM_SDK_PATH}/tools/ContentBuilder/builder/steamcmd.exe" 
}

def writeDepotVDF(String depotId, String buildOutputPath) {        
        writeFile file: "${STEAM_SDK_PATH}/tools/ContentBuilder/scripts/depot_${depotId}.vdf", 
                    text: """"DepotBuildConfig"
                            {
                                "DepotID" "${depotId}"
                                "contentroot" "${buildOutputPath}"
                                "FileMapping"
                                {
                                    "LocalPath" "*"
                                    "DepotPath" "."
                                    "recursive" "1"
                                }
                                "FileExclusion" "*.pdb"
                            }""".stripIndent()
}

def writeAppVDF(String appId, String depotId, String buildComment, String branch) {
        writeFile file: "${STEAM_SDK_PATH}/tools/ContentBuilder/scripts/app_${appId}.vdf", 
                    text: """"appbuild"
                            {
                                "appid" "${appId}"
                                "desc" "${env.BUILD_VERSION}        ${buildComment}"
                                "buildoutput" "${STEAM_SDK_PATH}/tools/ContentBuilder/output"
                                "contentroot" ""
                                "setlive" "${branch}"
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
        depotIds: [
            "Win64" : "12345",
            "Mac" : "12346",
        ],
        credentialsId: '',
        buildComment: '',
        buildOutputPath: ''
    ]
*/
    echo "Write Depot VDF"
    writeDepotVDF(config.depotId, config.buildOutputPath)
    
    echo "Write App VDF"
    writeAppVDF(config.appId, config.depotId, config.buildComment, config.branch)

    echo "upload"
    upload(config.appId, config.credentialsId)
}
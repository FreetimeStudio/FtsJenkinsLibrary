import net.freetimestudio.Platform

def getAppChannel(String targetPlatform) {
    if ( targetPlatform == Platform.Win64 ) {
        return  'windows'
    }
    if ( targetPlatform == Platform.Mac ) {
        return  'mac'
    }
    if ( targetPlatform == Platform.Linux ) {
        return  'linux'
    }
    
    return ''
}

def deploy(Map config = [:])
{
    //String target, String credentialsId, String user, String game
    
    def specificOutputFolder = platform.getOutputFolder(config.target)
    def appChannel = getAppChannel(config.target)
    
    withCredentials([string(credentialsId: config.credentialsId, variable: 'apiKey')]) {
        withEnv(["BUTLER_API_KEY=${apiKey}"]) {
            platform.executeScript("butler push \"${WORKSPACE}/Builds/${specificOutputFolder}\" \"${config.user}/${config.game}:${appChannel}\"", 'Upload')
        }
    }
}
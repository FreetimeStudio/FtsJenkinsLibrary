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

def deploy(String targetPlatform, String credentialsId, String user, String game)
{
    def specificOutputFolder = platform.getOutputFolder(targetPlatform)
    def appChannel = getAppChannel(targetPlatform)
    
    withCredentials([string(credentialsId: credentialsId, variable: 'apiKey')]) {
        withEnv(["BUTLER_API_KEY=${apiKey}"]) {
            platform.executeScript("butler push \"${WORKSPACE}/Builds/${specificOutputFolder}\" \"${user}\"/\"${game}\:${appChannel}", 'Upload', platforms.Win64)
        }
    }
}
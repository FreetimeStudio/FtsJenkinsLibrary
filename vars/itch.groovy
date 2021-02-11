import net.freetimestudio.Platform

//Uses env.ITCH_USER
//Uses env.ITCH_GAME

def getAppChannel(String platform) {
    if ( platform == Platform.Win64 ) {
        return  'windows'
    }
    if ( platform == Platform.Mac ) {
        return  'mac'
    }
    if ( platform == Platform.Linux ) {
        return  'linux'
    }
    
    return ''
}

def deploy(String platform, String credentialsId)
{
    def specificOutputFolder = ue4.getPlatformSpecificOutFolder(platform)
    def appChannel = getAppChannel(platform)
    
    withCredentials([string(credentialsId: credentialsId, variable: 'apiKey')]) {
        withEnv(["BUTLER_API_KEY=${apiKey}"]) {
            platform.executeScript("butler push \"${WORKSPACE}/Builds/${specificOutputFolder}\" \"${env.ITCH_USER}\"/\"${env.ITCH_GAME}\:${appChannel}", 'Upload', platforms.Win64)
        }
    }
}
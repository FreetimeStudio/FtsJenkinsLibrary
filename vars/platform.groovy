import net.freetimestudio.Platform
import net.freetimestudio.BuildResult

def executeScript(String command, String label)
{
    if(isUnix()) {
        sh(script: command, label: label)
        return 
    }
    
    bat(script: command, label: label) 
}

def getBuildNodeLabel(String targetPlatform) {
    if(targetPlatform == Platform.Win64) {
        return 'windows'
    }

    if(targetPlatform == Platform.Mac) {
        return 'mac'
    }

    if(targetPlatform == Platform.iOS) {
        return 'mac'
    }

    if(targetPlatform == Platform.Linux) {
        return 'linux'
    }

    if(targetPlatform == Platform.Switch) {
        return 'switch'
    }
    
    if(targetPlatform == Platform.PS4) {
        return 'ps4'
    }
    
    if(targetPlatform == Platform.XboxOne) {
        return 'xboxone'
    }
    
    return 'invalid'
}

def getOutputFolder(String targetPlatform) {
    if ( targetPlatform == Platform.Win64 ) {
        return  'WindowsNoEditor'
    }
    
    if ( targetPlatform == Platform.Mac ) {
        return  'MacNoEditor'
    }
    
    if ( targetPlatform == Platform.Linux ) {
        return  'LinuxNoEditor'
    }
    
    if ( targetPlatform == Platform.Switch ) {
        return  'SwitchNoEditor'
    }
    
    return ''
}



def getPlatformEmoji(String targetPlatform) {

    if ( targetPlatform == Platform.Win64 ) {
        return  "${env.WINDOWS_EMOJI}"
    }
    
    if ( targetPlatform == Platform.Mac ) {
        return  "${env.MAC_EMOJI}"
    }
    
    if ( targetPlatform == Platform.Linux ) {
        return  "${env.LINUX_EMOJI}"
    }

    if ( targetPlatform == Platform.Switch ) {
        return  "${env.SWITCH_EMOJI}"
    }

    if ( targetPlatform == Platform.Oculus ) {
        return  "${env.OCULUS_EMOJI}"
    }

    if ( targetPlatform == Platform.Steam ) {
        return  "${env.STEAM_EMOJI}"
    }

    if ( targetPlatform == Platform.Itch ) {
        return  "${env.ITCH_EMOJI}"
    }
    
    return ''
}

def getBuildResultEmoji(String buildResult) {
    return ''

    if ( buildResult == BuildResult.Success ) {
        return  "${env.BUILD_SUCCESS_EMOJI}"
    }
    
    if ( buildResult == BuildResult.Unstable ) {
        return  "${env.BUILD_UNSTABLE_EMOJI}"
    }
    
    if ( buildResult == BuildResult.Failure ) {
        return  "${env.BUILD_FAILURE_EMOJI}"
    }
    
    return ''
}

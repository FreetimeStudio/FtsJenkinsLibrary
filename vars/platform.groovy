import net.freetimestudio.Platform
import net.freetimestudio.BuildResult

def executeScript(String command, String label, String targetPlatform)
{
    echo "${command}"
    echo "${label}"
    echo "${targetPlatform}"

    if(targetPlatform == Platform.Mac || targetPlatform == Platform.iOS) {
        sh(script: command, label: label) 
    }
    
    if(targetPlatform == Platform.Win64 || platform == Platform.Linux || Platform == platforms.Switch) {
        bat(script: command, label: label) 
    }
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
    
    if ( targetPlatform == Platform.Linux ) {
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

    if ( targetPlatform == Platform.Steam ) {
        return  "${env.STEAM_EMOJI}"
    }

    if ( targetPlatform == Platform.Itch ) {
        return  "${env.ITCH_EMOJI}"
    }
    
    return ''
}

def getBuildResultEmoji(String buildResult) {
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

import net.freetimestudio.Platform
import net.freetimestudio.BuildResult

def isNodeWithLabelOnline(String label) {
    nodeList = nodesByLabel label: label, offline: false
    return !nodeList.isEmpty();
}

def executeScript(String command, String label, Boolean returnStdout = false)
{
    if(isUnix()) {
        return sh(script: command, label: label, returnStdout: returnStdout)
    }
    
    return bat(script: command, label: label, returnStdout: returnStdout)
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

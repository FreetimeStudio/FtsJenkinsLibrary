import net.freetimestudio.Platform
import net.freetimestudio.BuildResult

def executeScript(String command, String label, String platform)
{
    if(platform == Platform.Mac || platform == Platform.iOS) {
        sh(script: command, label: label) 
    }
    
    if(platform == Platform.Win64 || platform == Platform.Linux || Platform == platforms.Switch) {
        bat(script: command, label: label) 
    }
}

def getPlatformEmoji(String platform) {
    if ( platform == Platform.Win64 ) {
        return  "${env.WINDOWS_EMOJI}"
    }
    
    if ( platform == Platform.Mac ) {
        return  "${env.MAC_EMOJI}"
    }
    
    if ( platform == Platform.Linux ) {
        return  "${env.LINUX_EMOJI}"
    }

    if ( platform == Platform.Steam ) {
        return  "${env.STEAM_EMOJI}"
    }

    if ( platform == Platform.Itch ) {
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

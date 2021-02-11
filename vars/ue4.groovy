import net.freetimestudio.Platform
import net.freetimestudio.LogVerbosity
import net.freetimestudio.BuildResult

// env.UPROJECT_PATH
// env.PROJECT_FILE
// env.BRANCH_NAME

def getBuildNodeLabel(String platform) {
    if(platform == Platform.Win64) {
        return 'windows'
    }

    if(platform == Platform.Mac) {
        return 'mac'
    }

    if(platform == Platform.iOS) {
        return 'mac'
    }

    if(platform == Platform.Linux) {
        return 'linux'
    }

    if(platform == Platform.Switch) {
        return 'switch'
    }
    
    if(platform == Platform.PS4) {
        return 'ps4'
    }
    
    if(platform == Platform.XboxOne) {
        return 'xboxone'
    }
    
    return 'invalid'
}

//Get the editor runtime for this platform
def getEditorPlatform(String platform) {
    if(platform == Platform.Win64) {
        return 'Win64'
    }

    if(platform == Platform.Mac) {
        return 'Mac'
    }

    if(platform == Platform.iOS) {
        return 'Mac'
    }

    if(platform == Platform.Linux) {
        return 'Win64'
    }

    if(platform == Platform.Switch) {
        return 'Win64'
    }
    
    if(platform == Platform.PS4) {
        return 'Win64'
    }
    
    if(platform == Platform.XboxOne) {
        return 'Win64'
    }
    
    return 'invalid'
}

def getUE4DirectoryFolder(String platform) {
    String uePath = "${UE_PATH}"
    
    echo "getUE4DirectoryFolder"
    echo uePath
    
    if ( platform == Platform.Switch
      || platform == Platform.PS4
      || platform == Platform.XboxOne
      ) {
        uePath = uePath + "/Source"
    }

    uePath = uePath + "/UE_${env.UE_VERSION}"
    return uePath
}

def getUATPath(String platform) {
    String uatPath = getUE4DirectoryFolder(platform) + "/Engine/Build/BatchFiles/RunUAT"
    
    if ( platform == Platform.Mac || platform == Platform.iOS ) {
        return uatPath + ".sh"
    }

    return uatPath + ".bat"
}

def getUBTPath(String platform) {
    String ubtPath = getUE4DirectoryFolder(platform) + "/Engine/Binaries"
    
    if ( platform == Platform.Mac || platform == Platform.iOS ) {
        String monoPath = getUE4DirectoryFolder(platform) + "/Engine/Build/BatchFiles/Mac/RunMono.sh"

        ubtPath = monoPath + "\" \"" + ubtPath
    }

    return ubtPath + "/DotNET/UnrealBuildTool.exe"
}

def getUE4ExePath(String platform) {
    String ue4ExePath = getUE4DirectoryFolder(platform) + "/Engine/Binaries"
    
    if ( platform == Platform.Mac || platform == Platform.iOS ) {
        return ue4ExePath + "/Mac/UE4Editor.app/Contents/MacOS/UE4Editor"
    }
    
    return 'UE4Editor-Cmd.exe'
}

def getEditorCMDPath(String platform) {
    String cmdPath = getUE4DirectoryFolder(platform) + "/Engine/Binaries"
    
    if ( platform == Platform.Mac || platform == Platform.iOS ) {
        return  cmdPath + "/Mac/UE4Editor-Cmd"
    }
    
    return  cmdPath + "/Win64/UE4Editor-Cmd.exe"
}

def getPlatformSpecificOutFolder(String platform) {
    if ( platform == Platform.Win64 ) {
        return  'WindowsNoEditor'
    }
    
    if ( platform == Platform.Mac ) {
        return  'MacNoEditor'
    }
    
    if ( platform == Platform.Linux ) {
        return  'LinuxNoEditor'
    }
    
    if ( platform == Platform.Linux ) {
        return  'SwitchNoEditor'
    }
    
    return ''
}


def getLogMessages(Integer maxWarningsToShow = 5, Integer maxErrorsToShow = 5) {
	def logUrl = env.BUILD_URL + 'consoleText'
	
	def response = httpRequest(
		url: logUrl,
		authentication: 'jenkins', 
		ignoreSslErrors: true
	)

	def log = response.content
	
	def warnings = []
	def errors = []

	//echo 'Build log: ' + log
	def logLines = log.split("\n")
	def warningIndex = 0
	def errorIndex = 0

	logLines.each{ line ->
		def lowerLine = line.toLowerCase()
		if(lowerLine.contains("error:") || lowerLine.contains(": error")) {
			errorIndex++
			if(errorIndex > maxErrorsToShow)
			{
				return
			}
			
			errors.add(line)
		}
	
	
		if(lowerLine.contains("warning:")) {
		
		    //Ignore build data missing warnings
		    if(lowerLine.contains("_BuiltData': Can't find file.".toLowerCase())) {
                return
		    }
		
			warningIndex++
			if(warningIndex > maxWarningsToShow)
			{
				return
			}
			
			warnings.add(line)
		}
	}
	
	if(warningIndex > maxWarningsToShow) {
		def remainingWarnings = warningIndex - maxWarningsToShow
		
		warnings.add("... and ${remainingWarnings} more")
	}
	
	if(errorIndex > maxErrorsToShow) {
		def remainingErrors = errorIndex - maxErrorsToShow
		
		errors.add("... and ${remainingErrors} more")
	}
	
	def attachments = []
	attachments.addAll(errors)
	attachments.addAll(warnings)
	return attachments
}

def buildEditorBinaries(String platform) {

    echo "buildEditorBinaries"

    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
    
        echo "UnrealBuildTool-${NODE_NAME}"

        String ubtPath = getUBTPath(platform)
        echo "${ubtPath}"
        
        String editorPlatform = getEditorPlatform(platform)
        echo "${editorPlatform}"
    

        //platform.executeScript("\"${ubtPath}\" Development ${editorPlatform} -Project=\"${env.UPROJECT_PATH}\" -TargetType=Editor -Progress -NoHotReloadFromIDE", 'Compile Editor Binaries', platform)
        platform.executeScript("bla", "bla2", "bla3")
    }
}

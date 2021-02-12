import net.freetimestudio.Platform
import net.freetimestudio.LogVerbosity
import net.freetimestudio.BuildResult

// env.UPROJECT_PATH
// env.PROJECT_FILE
// env.BRANCH_NAME

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

//Get the editor runtime for this platform
def getEditorPlatform(String targetPlatform) {
    if(targetPlatform == Platform.Win64) {
        return 'Win64'
    }

    if(targetPlatform == Platform.Mac) {
        return 'Mac'
    }

    if(targetPlatform == Platform.iOS) {
        return 'Mac'
    }

    if(targetPlatform == Platform.Linux) {
        return 'Win64'
    }

    if(targetPlatform == Platform.Switch) {
        return 'Win64'
    }
    
    if(targetPlatform == Platform.PS4) {
        return 'Win64'
    }
    
    if(targetPlatform == Platform.XboxOne) {
        return 'Win64'
    }
    
    return 'invalid'
}

def getUE4DirectoryFolder(String targetPlatform) {
    String uePath = "${UE_PATH}"
    
    echo "getUE4DirectoryFolder"
    echo uePath
    
    if ( targetPlatform == Platform.Switch
      || targetPlatform == Platform.PS4
      || targetPlatform == Platform.XboxOne
      ) {
        uePath = uePath + "/Source"
    }

    uePath = uePath + "/UE_${env.UE_VERSION}"
    return uePath
}

def getUATPath(String targetPlatform) {
    String uatPath = getUE4DirectoryFolder(targetPlatform) + "/Engine/Build/BatchFiles/RunUAT"
    
    if ( targetPlatform == Platform.Mac || targetPlatform == Platform.iOS ) {
        return uatPath + ".sh"
    }

    return uatPath + ".bat"
}

def getUBTPath(String targetPlatform) {
    String ubtPath = getUE4DirectoryFolder(targetPlatform) + "/Engine/Binaries"
    
    if ( targetPlatform == Platform.Mac || targetPlatform == Platform.iOS ) {
        String monoPath = getUE4DirectoryFolder(targetPlatform) + "/Engine/Build/BatchFiles/Mac/RunMono.sh"

        ubtPath = monoPath + "\" \"" + ubtPath
    }

    return ubtPath + "/DotNET/UnrealBuildTool.exe"
}

def getUE4ExePath(String targetPlatform) {
    String ue4ExePath = getUE4DirectoryFolder(targetPlatform) + "/Engine/Binaries"
    
    if ( targetPlatform == Platform.Mac || targetPlatform == Platform.iOS ) {
        return ue4ExePath + "/Mac/UE4Editor.app/Contents/MacOS/UE4Editor"
    }
    
    return 'UE4Editor-Cmd.exe'
}

def getEditorCMDPath(String targetPlatform) {
    String cmdPath = getUE4DirectoryFolder(targetPlatform) + "/Engine/Binaries"
    
    if ( targetPlatform == Platform.Mac || targetPlatform == Platform.iOS ) {
        return  cmdPath + "/Mac/UE4Editor-Cmd"
    }
    
    return  cmdPath + "/Win64/UE4Editor-Cmd.exe"
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

def buildEditorBinaries(String targetPlatform) {

    echo "buildEditorBinaries"

    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
    
        echo "UnrealBuildTool-${NODE_NAME}"

        String ubtPath = getUBTPath(targetPlatform)
        echo "${ubtPath}"
        
        String editorPlatform = getEditorPlatform(targetPlatform)
        echo "${editorPlatform}"
    

        platform.executeScript("\"${ubtPath}\" Development ${editorPlatform} -Project=\"${env.UPROJECT_PATH}\" -TargetType=Editor -Progress -NoHotReloadFromIDE", 'Compile Editor Binaries', targetPlatform)
    }
}


def uploadEditorBinaries(String targetPlatform) {
    if(targetPlatform == Platform.Win64) {
        if (fileExists('Binaries.zip')) {
            fileOperations([fileDeleteOperation(excludes: '', includes: 'Binaries.zip')])
        }

        zip(zipFile: 'Binaries.zip', archive: false, glob: '**/Binaries/**/*.dll,**/Binaries/**/*.target,**/Binaries/**/*.modules')

        ftpPublisher alwaysPublishFromMaster: true,
            continueOnError: false,
            failOnError: false,
            masterNodeName: '',
            paramPublish: null,
            publishers: [[
                configName: 'FreetimeStudio', 
                transfers: [[
                    asciiMode: false, cleanRemote: true, excludes: '', flatten: false, 
                    makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', 
                    remoteDirectory: "${env.PROJECT_FILE}/${env.BRANCH_NAME}", remoteDirectorySDF: false, 
                    removePrefix: '', sourceFiles: 'Binaries.zip']], 
                usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false]]

    }
}

def validateAssets(String targetPlatform) {
    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
        def editorCMD = getEditorCMDPath(targetPlatform)
        platform.executeScript("\"${editorCMD}\" \"${env.UPROJECT_PATH}\" -run=DataValidation", 'Validate Data', targetPlatform)
    }
}

def lintProject(String targetPlatform, String lintPath) {
    try {
        catchError(buildResult: BuildResult.Success, stageResult: BuildResult.Failure) { //Linter returns 2 for warnings
            //TODO -TreatWarningsAsErrors 
            def editorCMD = getEditorCMDPath(targetPlatform)
            platform.executeScript("\"${editorCMD}\" \"${env.UPROJECT_PATH}\" \"${lintPath}\" -run=Linter -json=LintReport.json", 'Validate Conventions', targetPlatform)
        }
    }
    finally  {
        def violationReport = ""
        def lintReport = readJSON(file: "${WORKSPACE}/UnrealProject/Saved/LintReports/LintReport.json")
        def violators = lintReport.Violators
        
        violators.each { violator -> 
            violationReport = violationReport + "\n"
            violationReport = violationReport + "Warning: Linting "+ violator.ViolatorFullName + "\n"
            def violations = violator.Violations
            violations.each { violation -> 
                violationReport = violationReport + violation.RuleRecommendedAction + "\n"
            }
        }

        println(violationReport)
    }
}

def package(String targetPlatform) {
    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
        String uatPath = getUATPath(targetPlatform)
        String ue4ExePath = getUE4ExePath(targetPlatform)
        platform.executeScript("\"${uatPath}\" -ScriptsForProject=\"${env.UPROJECT_PATH}\" BuildCookRun -nocompile -nocompileeditor -installed -nop4 -project=\"${UPROJECT_PATH}\" -cook -stage -archive -archivedirectory=\"${BUILD_OUTPUT_PATH}\" -package -clientconfig=${params.buildConfig} -ue4exe=\"${ue4ExePath}\" -prereqs -nodebuginfo -targetplatform=${targetPlatform} -build -utf8output -Pak -Rocket", 'Package Project', targetPlatform)
    }
    
    stash includes: 'Builds/**', name: targetPlatform
    dir('Builds') {
        deleteDir()
    }
}
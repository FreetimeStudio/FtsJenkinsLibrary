import net.freetimestudio.Platform
import net.freetimestudio.LogVerbosity
import net.freetimestudio.BuildResult
import groovy.xml.*
import groovy.json.*

def isUnreal5(Map config = [:]) {
	if(config.ueVersion.startsWith("5")) {
		return true
	}
	
	return false
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

    if(targetPlatform == Platform.Android) {
        return 'Win64'
    }

    if(targetPlatform == Platform.Switch) {
        return 'Win64'
    }
    
    if(targetPlatform == Platform.PS4) {
        return 'Win64'
    }
    
    if(targetPlatform == Platform.PS5) {
        return 'Win64'
    }
    
    if(targetPlatform == Platform.XboxOne) {
        return 'Win64'
    }
    
    return 'invalid'
}

def readUnrealVersion(String uprojectPath) {
    def projectJson = readJSON file: uprojectPath
    return projectJson['EngineAssociation']
}

def getUE4DirectoryFolder(Map config = [:]) {
    String uePath = "${UE_PATH}"
    
    if ( config.target == Platform.Switch
      || config.target == Platform.PS4
      || config.target == Platform.PS5
      || config.target == Platform.XboxOne
      ) {
        uePath = uePath + "/Source"
    }

    uePath = uePath + "/UE_${config.ueVersion}"
    return uePath
}

def getUATPath(Map config = [:]) {
    String uatPath = getUE4DirectoryFolder(config) + "/Engine/Build/BatchFiles/RunUAT"
    
    if (isUnix()) {
        return uatPath + ".sh"
    }

    return uatPath + ".bat"
}

def getUBTPath(Map config = [:]) {
    String ubtPath = getUE4DirectoryFolder(config) + "/Engine/Binaries"
    
	String extraPath = ""
	
	if(isUnreal5(config)) {
		extraPath = "UnrealBuildTool/"
	}
	
    if ( isUnix() ) {
		if(isUnreal5(config)) {
			return ubtPath + "ThirdParty/DotNET/6.0.302/mac-x64/dotnet \""+ubtPath+"/DotNET/UnrealBuildTool/UnrealBuildTool.dll\""
		}

        String monoPath = getUE4DirectoryFolder(config) + "/Engine/Build/BatchFiles/Mac/RunMono.sh"

        ubtPath = monoPath + "\" \"" + ubtPath
    }
	
    return ubtPath + "/DotNET/"+extraPath+"UnrealBuildTool.exe"
}

def getUnrealExePath(Map config = [:]) {
    String ue4ExePath = getUE4DirectoryFolder(config) + "/Engine/Binaries"
    
    if ( isUnix() ) {
		if(isUnreal5(config)) {
			return ue4ExePath + "/Mac/UnrealEditor.app/Contents/MacOS/UnrealEditor"
		}
	
        return ue4ExePath + "/Mac/UE4Editor.app/Contents/MacOS/UE4Editor"
    }
    
	if(isUnreal5(config)) {
		return 'UnrealEditor-Cmd.exe'
	}
	
    return 'UE4Editor-Cmd.exe'
}

def getEditorCMDPath(Map config = [:]) {
    String cmdPath = getUE4DirectoryFolder(config) + "/Engine/Binaries"
    
    if ( isUnix() ) {
		if(isUnreal5(config)) {
			return  cmdPath + "/Mac/UnrealEditor-Cmd"
		}
	
        return  cmdPath + "/Mac/UE4Editor-Cmd"
    }

	if(isUnreal5(config)) {
		return  cmdPath + "/Win64/UnrealEditor-Cmd.exe"
	}
	
    return  cmdPath + "/Win64/UE4Editor-Cmd.exe"
}

def buildEditorBinaries(Map config = [:]) {

    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
    
        String ubtPath = getUBTPath(config)
        String editorPlatform = getEditorPlatform(config.target)

        platform.executeScript(
            "\"${ubtPath}\" Development ${editorPlatform} -Project=\"${config.projectPath}\"" + 
                " -TargetType=Editor -Progress -NoHotReloadFromIDE", 
            'Compile Editor Binaries')
    }
}

def packageProject(Map config = [:]) {
    
    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
        String uatPath = getUATPath(config)
        String ueExePath = getUnrealExePath(config)
		
		String script = "\"${uatPath}\"" +
			" -ScriptsForProject=\"${config.projectPath}\"" +
			" BuildCookRun -nocompile -nocompileeditor -installed -nop4" +
			" -project=\"${config.projectPath}\"" + 
			" -cook -stage -archive" +
			" -archivedirectory=\"${config.buildOutputPath}\"" + 
			" -package -clientconfig=${config.buildConfig}" +
			" -targetplatform=${config.target} -build -utf8output -Pak -Rocket -prereqs -nodebuginfo"

        
		if(isUnreal5(config)) {
			script = script + " -unrealexe=\"${ueExePath}\""
		}
		else {
			script = script + " -ue4exe=\"${ueExePath}\""
		}
		
        platform.executeScript(script, 'Package Project')
    }
}

/*
    echo "stashing"
    
    stash includes: 'Builds/**', name: getUE4DirectoryFolder(config)

    echo "clean"

    dir('Builds') {
        deleteDir()
    }

    echo "done"
*/

def uploadEditorBinaries(Map config = [:]) {

    if(config.target != Platform.Win64 && config.target != Platform.Mac) {
        println("Invalid target platform ${config.target} to upload binaries for")
        return
    }

    if (fileExists('Binaries.zip')) {
        fileOperations([fileDeleteOperation(excludes: '', includes: 'Binaries.zip')])
    }

    if(config.target == Platform.Win64) {    
        zip(zipFile: 'Binaries.zip', archive: false, 
            glob: '**/Binaries/**/*.dll,**/Binaries/**/*.target,**/Binaries/**/*.modules')
    }
    else if(config.target == Platform.Mac) {    
        zip(zipFile: 'Binaries.zip', archive: false, 
            glob: '**/Binaries/**/*.dylib,**/Binaries/**/*.target,**/Binaries/**/*.modules')
    }
    
    //TODO sha1 of file and upload info to FTP
    
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
                remoteDirectory: "${config.remoteDirectory}/${config.target}", remoteDirectorySDF: false, 
                removePrefix: '', sourceFiles: 'Binaries.zip']], 
            usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false]]

}

def packagePlugin(Map config = [:]) {
    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
        String uatPath = getUATPath(config)
        String ueExePath = getUnrealExePath(config)
		
		String script = "\"${uatPath}\"" +
			" BuildPlugin" +
			" -Plugin=\"${config.pluginPath}\"" +
			" -CreateSubFolder" +
			" -Package=\"${config.buildOutputPath}\""

        platform.executeScript(script, 'Package Plugin')
    }
}


def validateAssets(Map config = [:]) {
    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
        def editorCMD = getEditorCMDPath(config)
        platform.executeScript("\"${editorCMD}\" \"${config.projectPath}\" -run=DataValidation", 'Validate Data')
    }
}

def runProjectTests(Map config = [:]) {
    lock(resource: "UnrealBuildTool-${NODE_NAME}") {
        def editorCMD = getEditorCMDPath(config)
        platform.executeScript("\"${editorCMD}\" \"${config.projectPath}\" -ExecCmds=\"Automation RunTests ${config.tests};Quit\" -unattended -NoSound -nopause -nosplash -NullRHI -testexit=\"Automation Test Queue Empty\" -log -stdout -fullstdoutlogoutput -ReportExportPath=\"${config.reportPath}\"", 'Run Project Automation Tests')
    }
}

def lintProject(Map config = [:]) {
    try {
        catchError(buildResult: BuildResult.Success, stageResult: BuildResult.Failure) { //Linter returns 2 for warnings
            //TODO -TreatWarningsAsErrors 
            def editorCMD = getEditorCMDPath(config)
            platform.executeScript(
                "\"${editorCMD}\" \"${config.projectPath}\" \"${config.lintPath}\"" + 
                " -run=Linter -json=LintReport.json", 
                'Validate Conventions')
        }
    }
    finally  {
        def violationReport = ""
        def lintReport = readJSON(file: config.lintLogFile)
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


//Credit for original code https://www.emidee.net/ue4/2018/11/13/UE4-Unit-Tests-in-Jenkins.html
def convertUnitTestsReport(String JsonFilePath, String XmlOutPath) {
    def json = readFile file: "${JsonFilePath}", encoding: "UTF-8"
    // Needed because the JSON is encoded in UTF-8 with BOM

    json = json.replace( "\uFEFF", "" );

    def xml_content = getJUnitXMLContentFromJSON( json )

    writeFile file: "${XmlOutPath}", text: xml_content.toString()
}

//Credit for original code https://www.emidee.net/ue4/2018/11/13/UE4-Unit-Tests-in-Jenkins.html
@NonCPS
def getJUnitXMLContentFromJSON( String json_content ) {
    def j = new JsonSlurper().parseText( json_content )
    
    def sw = new StringWriter()
    def builder = new MarkupBuilder( sw )

    builder.doubleQuotes = true
    builder.mkp.xmlDeclaration version: "1.0", encoding: "utf-8"

    builder.testsuites( time: j.totalDuration ) {
		builder.testsuite( tests: j.succeeded + j.failed, failures: j.failed, time: j.totalDuration ) {
			for ( test in j.tests ) {
				builder.testcase( name: test.testDisplayName, classname: test.fullTestPath, status: test.state, time: test.duration ) {
					if(test.state == "Fail") {
						def errorMessages = ""
						for ( entry in test.entries ) {
							if (entry.event.type == "Error") {
								errorMessages = "${errorMessages} ${entry.event.message}\n"
							}
						}

						//echo "Warning: Failed Automation Test \"${test.testDisplayName}\": ${errorMessages}" 
						builder.failure( message: errorMessages, type: "Error", "" )
					}
				}
			}
		} 
	}
	
    return sw.toString()
}


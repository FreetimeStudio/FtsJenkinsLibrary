import net.freetimestudio.LogVerbosity

//Needs DISCORD_WEBHOOK environment variable 
//BUILD_FRIENDLY_NAME
//SLACK_CHANNEL

def setLogVerbosity(Integer newVerbosity) {
    env.LOG_VERBOSITY = newVerbosity
}

def setLogVerbosity(String newVerbosity) {
    setLogVerbosity(LogVerbosity.Success)
    
    if(newVerbosity == 'Warning'){
        setLogVerbosity(LogVerbosity.Warning)
    }
    if(newVerbosity == 'Error'){
        setLogVerbosity(LogVerbosity.Error)
    }
    if(newVerbosity == 'Log'){
        setLogVerbosity(LogVerbosity.Log)
    }
    if(newVerbosity == 'Verbose'){
        setLogVerbosity(LogVerbosity.Verbose)
    }
}

def getLogVerbosity(Integer newVerbosity) {
    return env.LOG_VERBOSITY as Integer
}

def verbosityVerbose() {
    return LogVerbosity.Verbose
}

def verbosityLog() {
    return LogVerbosity.Log
}

def verbositySuccess() {
    return LogVerbosity.Success
}

def verbosityWarning() {
    return LogVerbosity.Warning
}

def verbosityError() {
    return LogVerbosity.Error
}

def parseLog(String rulesPath) {
	step([$class: 'LogParserPublisher', parsingRulesPath: "${rulesPath}", useProjectRule: false, unstableOnWarning: true])
}

def matchesAnyPattern(String text, String[] ignorePatterns)
{
    Boolean result = false
    
    ignorePatterns.each{ pattern ->
        if(matchesPattern(text, pattern)) {
            result = true
            break
        }
    }
    
    return result
}

def matchesPattern(String text, String pattern)
{
    def lowerText = text.toLowerCase()
    def lowerPattern = pattern.toLowerCase()
    if(text.contains(lowerPattern)) {
        return true
    }
    
    return false
}

def getLogMessages(Integer maxWarningsToShow = 5, Integer maxErrorsToShow = 5, String[] ignorePatterns=[]) {
	def logUrl = env.BUILD_URL + 'consoleText'
	
	def response = httpRequest(
		url: logUrl,
		authentication: 'jenkins', 
		ignoreSslErrors: true
	)

	def log = response.content
	
	def errorPatterns = ["error:", ": error"]
	def warningPatterns = ["warning:", ": warning"]

	def warnings = []
	def errors = []

	def logLines = log.split("\n")
	def warningIndex = 0
	def errorIndex = 0

	logLines.each{ line ->
		if(matchesAnyPattern(line, errorPatterns) && !matchesAnyPattern(line,ignorePatterns)) {
			errorIndex++
			if(errorIndex > maxErrorsToShow)
			{
				return
			}
			
			errors.add(line)
		}
	
	
		if(matchesAnyPattern(line, warningPatterns) && !matchesAnyPattern(line,ignorePatterns)) {
		
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
	
	def messages = {}
	messages.errors = errors
	messages.warnings =warnings
	return messages
}

def sendMessage(Map config = [:])
{
    def defaultConfig = [
        title: '',
        message: '',
        target: 'Win64',
        verbosity: LogVerbosity.Log,
        emoji: '',
        attachments: []
    ]
    
    def params = defaultConfig << config

    if(getLogVerbosity() == LogVerbosity.None)
    {
        return
    }
    
    if(getLogVerbosity() < params.verbosity)
    {
        println("Not sending message\n${params.title}\n${params.message}")
        return
    }
    
    println("Sending message\n${params.title}\n${params.message}")

    if(env.SLACK_CHANNEL)
    {
        sendSlackMessage(params.title, params.message, params.target, params.verbosity, params.emoji, params.attachments)
    }
    
    if(env.DISCORD_WEBHOOK)
    {
        sendDiscordMessage(params.title, params.message, params.target, params.verbosity, params.emoji, params.attachments)
    }
}

def sendDiscordMessage(String title, String message, String targetPlatform, Integer verbosity, String extraEmoji = '', attachments = [])
{
    def messageColors = ['ABORTED', 'FAILURE', 'ABORTED', 'SUCCESS', 'ABORTED', 'ABORTED']
    
    def platformEmoji = platform.getPlatformEmoji(targetPlatform)
    def color = messageColors[verbosity]

    discordSend webhookURL: env.DISCORD_WEBHOOK, 
        title: "${title}",
        description: "${extraEmoji} ${platformEmoji} ${currentBuild.fullDisplayName} ${message}",
        //link: "${env.BUILD_URL}/parsed_console", //Disabled because Discord will not be able to verify an internal link and errors out
        result: color
    
    attachments.each{ attachment ->
        def attachmentMessage = formatAttachmentForDiscord(attachment)
        discordSend webhookURL: env.DISCORD_WEBHOOK, description: attachmentMessage.description, result: attachmentMessage.result, title: attachmentMessage.title
    }
}


def sendSlackMessage(String title, String message, String targetPlatform, Integer verbosity, String extraEmoji = '', attachments = [])
{
    def messageColors = ['neutral', 'danger', 'warning', 'good', 'neutral', 'neutral']
    
    def platformEmoji = platform.getPlatformEmoji(targetPlatform)
    def color = messageColors[verbosity]
    def slackAttachments = formatAttachmentsForSlack(attachments)
    
    slackSend(channel: "#${env.SLACK_CHANNEL}",
        color: color,
        message: "${extraEmoji} ${platformEmoji} <${env.BUILD_URL}/parsed_console|${currentBuild.fullDisplayName}>: ${title} ${message}",
        attachments: slackAttachments)
}

def formatAttachmentsForSlack(unformattedAttachments) {
    def slackAttachments = []
    
    unformattedAttachments.each{ unformattedAttachment ->
        def text = unformattedAttachment.message
        def color = 'neutral'
        
        if(unformattedAttachment.type == "warning") {
            color = 'warning'
        }
        else if(unformattedAttachment.type == "error") {
            color = 'danger'
        }
        
        slackAttachments.add([
            text: text,
            fallback: text,
            color: color
        ])
    }

    return slackAttachments
}

def formatAttachmentForDiscord(unformattedAttachment) {
    def message = {
        title: 'undefined'
        description: 'undefined'
        result: 'ABORTED'
    }


    message.description = unformattedAttachment.message

    if(unformattedAttachment.type == "warning") {
        message.title = 'Warning'
        message.result = 'UNSTABLE'
    }
    else if(unformattedAttachment.type == "error") {
        message.title = 'Error'
        message.result = 'FAILURE'
    }
    
    return message
}


def getLogMessageAttachments(Integer maxWarningsToShow = 5, Integer maxErrorsToShow = 5, String[] ignorePatterns=[]) {

    def attachments = []
    
    def logMessages = getLogMessages(maxWarningsToShow, maxErrorsToShow, String[] ignorePatterns)
    logMessages.warnings.each{ warning -> 
        println("Warning to send: ${warning}")
        attachments.add([
            type: 'warning',
            message: warning
          ])
    }
        
    logMessages.errors.each{ error -> 
        println("Error to send: ${error}")
        attachments.add([
            type: 'error',
            message: error
          ])
    }

    return attachments
}

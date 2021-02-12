import net.freetimestudio.LogVerbosity

//Needs DISCORD_WEBHOOK environment variable 
//BUILD_FRIENDLY_NAME
//SLACK_CHANNEL

def sendMessage(String title, String message, String targetPlatform, Integer verbosity, String extraEmoji = '', attachments = [])
{
    if(env.SLACK_CHANNEL != None)
    {
        sendSlackMessage(title, message, targetPlatform, verbosity, extraEmoji, attachments)
    }
    
    if(env.DISCORD_WEBHOOK != None)
    {
        sendDiscordMessage(title, message, targetPlatform, verbosity, extraEmoji, attachments)
    }
}

def sendDiscordMessage(String title, String message, String targetPlatform, Integer verbosity, String extraEmoji = '', attachments = [])
{
    def messageColors = ['ABORTED', 'FAILURE', 'ABORTED', 'SUCCESS', 'ABORTED', 'ABORTED']
    
    if(env.LOG_VERBOSITY == LogVerbosity.None)
    {
        return
    }
    
    if(env.LOG_VERBOSITY < verbosity)
    {
        println("not sending message\n${title}\n${message}")
        return
    }
    
    def platformEmoji = platform.getPlatformEmoji(targetPlatform)
    def color = messageColors[verbosity]

    println("sending message\n${title}\n${message}")
    discordSend webhookURL: "${env.DISCORD_WEBHOOK}", description: "${extraEmoji} ${platformEmoji} ${currentBuild.fullDisplayName} ${message}", result: color, title: "${title}"
    
    attachments.each{ attachment ->
        def message = formatAttachmentForDiscord(attachment)
        discordSend webhookURL: "${env.DISCORD_WEBHOOK}", description: message.description, result: message.result, title: message.title
    }
}


def sendSlackMessage(String title, String message, String targetPlatform, Integer verbosity, String extraEmoji = '', attachments = [])
{
    def messageColors = ['neutral', 'danger', 'warning', 'good', 'neutral', 'neutral']
    
    if(env.LOG_VERBOSITY == LogVerbosity.None)
    {
        return
    }
    
    if(env.LOG_VERBOSITY < verbosity)
    {
        println("not sending message\n${title}\n${message}")
        return
    }
    
    def platformEmoji = platform.getPlatformEmoji(targetPlatform)
    def color = messageColors[verbosity]

    println("sending message\n${title}\n${message}")
    slackSend channel: "#${env.SLACK_CHANNEL}",
        color: color,
        message: "${extraEmoji} ${platformEmoji} <${env.BUILD_URL}/parsed_console|${env.BUILD_FRIENDLY_NAME}>: ${title} ${currentBuild.fullDisplayName} ${message}"
        attachments: formatAttachmentsForSlack(attachments)
}

def formatAttachmentsForSlack(unformattedAttachments) {
    def slackAttachments = []
    
    unformattedAttachments.warnings.each{ unformattedAttachment ->
        def text = unformattedAttachment.message
        def color = 'neutral'
        
        if(unformattedAttachment.type = "warning") {
            color = 'warning'
        }
        else if(unformattedAttachment.type = "error") {
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

    if(unformattedAttachment.type = "warning") {
        message.title = 'Warning'
        message.result = 'UNSTABLE'
    }
    else if(unformattedAttachment.type = "error") {
        message.title = 'Error'
        message.result = 'FAILURE'
    }
    
    return message
}


def getLogMessageAttachments(Integer maxWarningsToShow = 5, Integer maxErrorsToShow = 5) {

    def attachments = []
    
    def logMessages = ue4.getLogMessages(maxWarningsToShow, maxErrorsToShow)
    logMessages.warnings.each{ warning -> 
        attachments.add([
            type: 'warning',
            message: warning
          ])
    }
        
    logMessages.errors.each{ error -> 
        attachments.add([
            type: 'error',
            message: error
          ])
    }

    return attachments
}

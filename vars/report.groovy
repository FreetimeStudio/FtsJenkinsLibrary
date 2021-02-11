import net.freetimestudio.LogVerbosity

//Needs DISCORD_WEBHOOK environment variable 
//BUILD_FRIENDLY_NAME
//SLACK_CHANNEL

def sendMessage(String title, String message, String platform, Integer verbosity, String extraEmoji = '')
{
    if(env.SLACK_CHANNEL != None)
    {
        sendSlackMessage(title, message, platform, verbosity, extraEmoji)
    }
    
    if(env.DISCORD_WEBHOOK != None)
    {
        sendDiscordMessage(title, message, platform, verbosity, extraEmoji)
    }
}

def sendDiscordMessage(String title, String message, String platform, Integer verbosity, String extraEmoji = '')
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
    
    def platformEmoji = getPlatformEmoji(platform)
    def color = messageColors[verbosity]

    println("sending message\n${title}\n${message}")
    discordSend webhookURL: "${env.DISCORD_WEBHOOK}", description: "${extraEmoji} ${platformEmoji} ${currentBuild.fullDisplayName} ${message}", result: color, title: "${title}"
}


def sendSlackMessage(String title, String message, String platform, Integer verbosity, String extraEmoji = '')
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
    
    def platformEmoji = getPlatformEmoji(platform)
    def color = messageColors[verbosity]

    println("sending message\n${title}\n${message}")
    slackSend channel: "#${env.SLACK_CHANNEL}",
        color: color,
        message: "${extraEmoji} ${platformEmoji} <${env.BUILD_URL}/parsed_console|${env.BUILD_FRIENDLY_NAME}>: ${title} ${currentBuild.fullDisplayName} ${message}"
}

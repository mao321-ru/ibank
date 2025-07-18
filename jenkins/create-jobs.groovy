// Создает пайплайны в Jenkins по файлам jenkins.job.xml проекта.
// Скрипт выполняется в Script Console (путь "/script" в интерфейсе), перед выполнением нужно
// указать фактические значения в basePath и gitRepoUrl.
//

// Абсолютный путь к checkout репозатирия проекта
def basePath = "C:/Users/Alexey/home/mao/java/ibank"

// Абсолютный путь к git-репозитарию для использования с консольным git
def gitRepoUrl = "file:///cygdrive/c/Users/Alexey/home/user/java/ibank/.git"

// Список модулей (подкаталогов) сервисов (пустая строка для корневого job)
def serviceSuffixes = [
    "keycloak",
    "kafka",
    "postgres",
    "accounts-service",
    "blocker-service",
    "cash-service",
    "exchange-service",
    "exrate-service",
    "front-service",
    "notify-service",
    "transfer-service",
    ""
]

def oldGitRepoUrl = "file:///cygdrive/c/Users/Alexey/home/user/java/ibank/.git"

serviceSuffixes.each { suffix ->
    def baseName = suffix.replace( "-service", "")
    def jobName = baseName ? "IBank_${baseName}" : "IBank"
    def isBuilded = suffix != baseName
    def configPath =
        isBuilded ?  "${basePath}/jenkins/builded-service/job.xml" :
        suffix ? "${basePath}/jenkins/${suffix}/job.xml" :
        "${basePath}/jenkins/job.xml"

    def xml = new File(configPath).text
    xml = xml.replace(oldGitRepoUrl, gitRepoUrl)

    if( isBuilded) {
        xml = xml.replace( " Front ", " " + baseName.capitalize() + " ")
    }

    if ( Jenkins.instance.getItem(jobName) != null) {
        println "Job '${jobName}' already exists. Skipping creation."
    }
    else {
        Jenkins.instance.createProjectFromXML(jobName, new ByteArrayInputStream(xml.bytes))
        println "Created job: ${jobName} from ${configPath}"
    }
}

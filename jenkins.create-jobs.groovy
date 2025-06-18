// Создает пайплайны в Jenkins по файлам jenkins.job.xml проекта.
// Скрипт выполняется в Script Console (путь "/script" в интерфейсе), перед выполнением нужно
// указать фактические значения в basePath и gitRepoUrl.
//

// Абсолютный путь к checkout репозатирия проекта
def basePath = "C:/Users/Alexey/home/mao/java/ibank"

// Абсолютный путь к git-репозитарию для использования с консольным git
def gitRepoUrl = "file:///cygdrive/c/Users/Alexey/home/user/java/ibank/.git"

// Список модулей (подкаталогов) сервисов для создания джобов (пустая строка для корневого job)
def serviceSuffixes = [
    "keycloak",
    "postgres",
    "front-service",
    ""
]

def oldGitRepoUrl = "file:///cygdrive/c/Users/Alexey/home/user/java/ibank/.git"

serviceSuffixes.each { suffix ->
    def baseName = suffix.replace( "-service", "")
    def jobName = baseName ? "IBank_${baseName}" : "IBank"
    def configPath = suffix ?
        "${basePath}/${suffix}/jenkins.job.xml" :
        "${basePath}/jenkins.job.xml"

    def xml = new File(configPath).text
    xml = xml.replace(oldGitRepoUrl, gitRepoUrl)

    if ( Jenkins.instance.getItem(jobName) != null) {
        println "Job '${jobName}' already exists. Skipping creation."
    }
    else {
        Jenkins.instance.createProjectFromXML(jobName, new ByteArrayInputStream(xml.bytes))
        println "Created job: ${jobName} from ${configPath}"
    }
}

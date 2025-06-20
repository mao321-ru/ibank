// Удаляет пайплайны проекта в Jenkins
// Скрипт выполняется в Script Console (путь "/script" в интерфейсе)

// Список модулей (подкаталогов) сервисов (пустая строка для корневого job)
def serviceSuffixes = [
    "keycloak",
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

serviceSuffixes.each { suffix ->
    def baseName = suffix.replace( "-service", "")
    def jobName = baseName ? "IBank_${baseName}" : "IBank"

    def job = Jenkins.instance.getItem(jobName)

    if (job) {
        try {
            job.delete()
            println "Job '${jobName}' successfully deleted"
        } catch (Exception e) {
            println "ERROR: Failed to delete job '${jobName}': ${e.message}"
        }
    } else {
        println "Job '${jobName}' not found. Skipping..."
    }
}


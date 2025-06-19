{{- define "srv.testConnection" -}}
apiVersion: v1
kind: Pod
metadata:
  name: "{{ include "srv.fullname" . }}-test-connection"
  labels:
    {{- include "srv.labels" . | nindent 4 }}
  annotations:
    "helm.sh/hook": test
spec:
  containers:
    - name: wget
      image: busybox
      command: ['wget']
      args: ['{{ include "srv.fullname" . }}:{{ .Values.service.port }}']
  restartPolicy: Never
{{- end -}}

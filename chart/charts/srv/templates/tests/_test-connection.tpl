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
      command: ['sh', '-c']
      args:
        - |
          wget -S -O /dev/null "{{ include "srv.fullname" . }}:{{ .Values.service.port }}" 2>&1 |
            grep "HTTP/1.1 \(200\|401\)"
  restartPolicy: Never
{{- end -}}

{{- define "srv.service" -}}
apiVersion: v1
kind: Service
metadata:
  name: {{ include "srv.fullname" . }}
  labels:
    {{- include "srv.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: http
      protocol: TCP
      name: http
  selector:
    {{- include "srv.selectorLabels" . | nindent 4 }}
{{- end -}}

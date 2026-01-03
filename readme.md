# CloudSystemMQ (Master + Node)

Ein kleines **Master/Node-Orchestrierungs-System** für Game-Server-Instanzen (Templates), das über **ActiveMQ (JMS)** kommuniziert.

- **Node**: startet/stoppt Game-Server-Prozesse aus Templates, sendet Heartbeats + Events.
- **Master**: nimmt Events entgegen, speichert Node-/Server-Zustand in einer DB, bietet REST-API und reconciled per Scheduler anhand von Template-Policies.

---

## Features

### Node
- Heartbeat (alle ~5s) mit CPU/RAM/Serverstatus
- Command Consumer über `node.<nodeId>.commands`
- Server Lifecycle Management (Start/Stop aus Templates)
- Port-Management (30000–31000)

### Master
- Event Subscriber (`cloud.events` Topic)
- REST API (Nodes, Server, Template Policies)
- Scheduler für Auto-Scaling anhand von Policies
- Node-Auswahl per Score (RAM/CPU/Last)

---

## Starten

```bash
# Node + Master
java -jar app.jar

# nur Master
java -jar app.jar --master

# nur Node
java -jar app.jar --node
```

---

## Konfiguration (ENV / JVM Props)

- ACTIVEMQ_BROKER_URL (default: tcp://localhost:61616)
- ACTIVEMQ_USER (default: admin)
- ACTIVEMQ_PASSWORD (default: admin)
- MASTER_URL (Node, default: http://localhost:8080)

---

## REST API (Master)

Base URL: `/api`

### Nodes
- GET /nodes
- GET /nodes/{id}
- POST /nodes/{id}/shutdown

### Servers
- GET /servers
- GET /servers/template/{template}
- POST /servers/request/{template}
- POST /servers/{uuid}/stop
- POST /servers/{uuid}/update/{status}

### Template Policies
- GET /template-policies
- POST /template-policies
- PUT /template-policies/{id}
- DELETE /template-policies/{id}

---

## Templates

Verzeichnis: `templates/servers/<TemplateName>`

Aktuell:
- LOBBY
- BOW_BASH
- GUESS_IT
- HIDE_AND_SEEK

Beim Start wird das Template nach `running/<uuid>` kopiert.

---

## Architektur

- ActiveMQ Topic: `cloud.events` (Node -> Master)
- ActiveMQ Queue: `node.<nodeId>.commands` (Master -> Node)

---

## Lizenz

TBD

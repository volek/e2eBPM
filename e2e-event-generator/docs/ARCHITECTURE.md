# Архитектура генератора

```mermaid
flowchart TB
    subgraph CONFIG["Configuration"]
        APP["application.yml"]
        SC["Scenario YAML"]
        BPMN["BPMN resources"]
    end

    subgraph APPGEN["Generator"]
        RUN["GeneratorRunner"]
        SE["ScenarioEngine"]

        MG["FlowModelGenerator"]
        IG["FlowInstanceGenerator"]
        LG["LegacyInstanceGenerator"]

        HF["HeaderFactory"]
        CV["ContractValidator"]
        KP["KafkaPublisher"]
    end

    subgraph KAFKA["Kafka"]
        MT["Flow Model Topic"]
        IT["Flow Instance Topic"]
        LT["Legacy Topic"]
    end

    APP --> RUN
    SC --> SE
    BPMN --> MG

    RUN --> SE

    SE --> MG
    SE --> IG
    SE --> LG

    MG --> HF
    IG --> HF
    LG --> HF

    MG --> CV
    IG --> CV
    LG --> CV

    CV --> KP

    KP --> MT
    KP --> IT
    KP --> LT
```

## Границы

Generator:
- не зависит от BAMN source code;
- не пишет в PostgreSQL;
- не обращается к ABYSS;
- не вычисляет E2E_ID;
- не исполняет BPMN.

Generator создаёт только входную телеметрию для POC.

package ru.sber.bamn.e2egenerator.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "generator")
class GeneratorProperties {
    var command: String = "run"
    var scenario: String = "full-poc"
    var mode: String = "BATCH"
    var seed: Long = 1001
    var continuousDelayMs: Long = 5000

    var kafka: Kafka = Kafka()
    var headers: Headers = Headers()

    class Kafka {
        var topics: Topics = Topics()

        class Topics {
            var flowModels: String = "model-update-event"
            var flowInstances: String = "instance-update-event"
            var legacyInstances: String = "legacy-events"
        }
    }

    class Headers {
        var flowModel: Header = Header()
        var flowInstance: Header = Header()
        var legacyInstance: Header = Header()
        var ceSpecversion: String = ""

        class Header {
            var ceType: String = ""
            var ceSource: String = ""
        }
    }
}

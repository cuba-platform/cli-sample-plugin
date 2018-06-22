package com.haulmont.cli.tutorial

import com.google.common.eventbus.Subscribe
import com.haulmont.cuba.cli.CliPlugin
import com.haulmont.cuba.cli.event.InitPluginEvent

class DemoPlugin : CliPlugin {
    @Subscribe
    fun onInit(event: InitPluginEvent) {
        event.commandsRegistry {
            command("idea", IdeaOpenCommand())
        }
    }
}
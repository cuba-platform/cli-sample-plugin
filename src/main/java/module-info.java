import com.haulmont.cuba.cli.CliPlugin;

module cli.plugin.tutorial {
    requires java.base;
    requires kotlin.stdlib;
    requires kotlin.reflect;

    requires jcommander;

    requires com.haulmont.cuba.cli;
    requires com.google.common;
    requires kodein.di.core.jvm;
    requires kodein.di.generic.jvm;
    requires practicalxml;
    
    opens com.haulmont.cli.tutorial;
    exports com.haulmont.cli.tutorial;

    provides CliPlugin with com.haulmont.cli.tutorial.DemoPlugin;
}
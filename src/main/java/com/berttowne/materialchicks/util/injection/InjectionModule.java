package com.berttowne.materialchicks.util.injection;

import com.berttowne.materialchicks.MaterialChicks;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class InjectionModule extends AbstractModule {

    private final MaterialChicks plugin;

    public InjectionModule(MaterialChicks plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        AppInjector.getServices(Module.class).forEach(this::install);

        bind(MaterialChicks.class).toInstance(this.plugin);
        bind(Server.class).toInstance(Bukkit.getServer());
    }

}